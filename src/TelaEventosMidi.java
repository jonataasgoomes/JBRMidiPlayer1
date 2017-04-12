import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import javax.swing.*;
import java.awt.*;

/**
 * Created by Ricardo on 09/04/2017.
 */

public class TelaEventosMidi extends JFrame {
    private LeitorEventosMidi leitor;
    private JTable tabelaEventosMidi;
    private Dimension tamanhoDaJanela = new Dimension(800, 600);
    private String[][] dadosTabela = {null};

    public TelaEventosMidi(Track[] trilhas, double durTique) {
        super("Mensagens MIDI");
        this.leitor = new LeitorEventosMidi(trilhas, durTique);
        configuraTelaEventos();
        configuraTabela();

        setVisible(true);
    }

    private void configuraTelaEventos() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(tamanhoDaJanela);
    }

    private void configuraTabela() {
        String[] colunasTabela = {"Trilha", "Instante (tiques)", "Instante (segundos)",
                                    "Op. 1", "Op. 2", "Op. 3", "Código", "Mensagem"};
        leitor.obtemDadosTabela();
        tabelaEventosMidi = new JTable(dadosTabela, colunasTabela);
        tabelaEventosMidi.setFillsViewportHeight(true);
        tabelaEventosMidi.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(tabelaEventosMidi));
    }

    private class LeitorEventosMidi {
        Track[] trilhas;
        private int indiceTrilhaAtual;
        private int indiceEventoAtual;
        private int totalLinhas;
        private double duracaoTique;

        private static final int MENSAGEM_NOTE_OFF     = 128;
        private static final int MENSAGEM_NOTE_ON      = 144;
        private static final int MENSAGEM_POLY_KEY     = 160;
        private static final int MENSAGEM_CTRL_CHANGE  = 176;
        private static final int MENSAGEM_PROG_CHANGE  = 192;
        private static final int MENSAGEM_CHAN_PRES    = 208;
        private static final int MENSAGEM_PITCH_BEND   = 224;

        private LeitorEventosMidi(Track[] trilhas, double durTique) {
            if (trilhas != null) {
                this.trilhas = trilhas;
            } else {
                this.trilhas = new Track[0];
            }
            duracaoTique = durTique;
            totalLinhas = 0;
            for (Track trilha : trilhas) {
                totalLinhas += trilha.size();
            }
            reiniciar();
        }

        private String[] obtemProximaLinha() {
            if (indiceTrilhaAtual < trilhas.length) {
                Track trilhaAtual = trilhas[indiceTrilhaAtual];
                int size = trilhaAtual.size();
                if (indiceEventoAtual < size) {
                    MidiEvent eventoAtual = trilhaAtual.get(indiceEventoAtual++);
                    return processaEvento(eventoAtual);
                } else {
                    // Chegou ao final da trilha, passa para proxima
                    indiceTrilhaAtual++;
                    indiceEventoAtual = 0;
                    return obtemProximaLinha();
                }
            }
            return null;
        }

        private String[] processaEvento(MidiEvent evento) {
            MidiMessage mensagem = evento.getMessage();
            byte bytes_mensagem[] = mensagem.getMessage();
            int status = mensagem.getStatus();
            StringBuilder msg_status_sb = new StringBuilder();
            switch (status & 0xF0) {
                case MENSAGEM_NOTE_OFF:
                    msg_status_sb.append("[Note OFF] ").append(converteNota(bytes_mensagem[1]));
                    break;
                case MENSAGEM_NOTE_ON:
                    msg_status_sb.append("[Note ON] ").append(converteNota(bytes_mensagem[1]));
                    break;
                case MENSAGEM_POLY_KEY:
                    msg_status_sb.append("[Polyphonic key pressure]"); break;
                case MENSAGEM_CTRL_CHANGE:
                    msg_status_sb.append("[Control change]"); break;
                case MENSAGEM_PROG_CHANGE:
                    msg_status_sb.append("[Program change]"); break;
                case MENSAGEM_CHAN_PRES:
                    msg_status_sb.append("[Channel pressure]"); break;
                case MENSAGEM_PITCH_BEND:
                    msg_status_sb.append("[Pitch bend]"); break;
                default:
                    msg_status_sb.append((status == 0xFF) ? "[Meta mensagem]" : Integer.toString(status));
            }
            String[] linha = new String[8];
            linha[0] = Integer.toString(indiceTrilhaAtual); // Trilha
            linha[1] = Long.toString(evento.getTick()); // Instante (tiques)
            linha[2] = Double.toString(evento.getTick() * duracaoTique) + " s"; // Instante (segundos)
            linha[3] = bytes_mensagem.length > 1 ? Byte.toString(bytes_mensagem[1]) : "-"; // Op. 1
            linha[4] = bytes_mensagem.length > 2 ? Byte.toString(bytes_mensagem[2]) : "-"; // Op. 2
            linha[5] = bytes_mensagem.length > 3 ? Byte.toString(bytes_mensagem[3]) : "-"; // Op. 3
            linha[6] = Integer.toString(status); // Código
            linha[7] = msg_status_sb.toString(); // Mensagem
            return linha;
        }

        private void obtemDadosTabela() {
            dadosTabela = new String[totalLinhas][5];
            for (int i = 0; i < totalLinhas; ++i) {
                dadosTabela[i] = obtemProximaLinha();
            }
        }

        private String converteNota(Byte nota) {
            String s_nota;
            int nota_int = Byte.toUnsignedInt(nota);
            switch (nota_int % 12) {
                case 0:
                    s_nota = "Dó"; break;
                case 1:
                    s_nota = "Dó#"; break;
                case 2:
                    s_nota = "Ré"; break;
                case 3:
                    s_nota = "Ré#"; break;
                case 4:
                    s_nota = "Mi"; break;
                case 5:
                    s_nota = "Fá"; break;
                case 6:
                    s_nota = "Fá#"; break;
                case 7:
                    s_nota = "Sol"; break;
                case 8:
                    s_nota = "Sol#"; break;
                case 9:
                    s_nota = "Lá"; break;
                case 10:
                    s_nota = "Lá#"; break;
                case 11:
                    s_nota = "Si"; break;
                default:
                    s_nota = "";
            }
            return String.format("%s, %dª oitava", s_nota, (nota_int / 12) - 2);
        }

        private void reiniciar() {
            indiceTrilhaAtual = 0;
            indiceEventoAtual = 0;
        }

    }

}
