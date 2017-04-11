import javax.sound.midi.MetaMessage;
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
    private String[][] dadosTabela = {{"A", "B", "C", "D", "E"}};

    public TelaEventosMidi(Track[] trilhas) {
        super("Mensagens MIDI");
        this.leitor = new LeitorEventosMidi(trilhas);
        configuraTelaEventos();
        configuraTabela();

        setVisible(true);
    }

    private void configuraTelaEventos() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(tamanhoDaJanela);
    }

    private void configuraTabela() {
        String[] colunasTabela = {"Trilha", "Instante (tiques)", "Instante (segundos)", "Código", "Mensagem"};
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

        private static final int MENSAGEM_NOTE_OFF     = 128;
        private static final int MENSAGEM_NOTE_ON      = 144;
        private static final int MENSAGEM_POLY_KEY     = 160;
        private static final int MENSAGEM_CTRL_CHANGE  = 176;
        private static final int MENSAGEM_PROG_CHANGE  = 192;
        private static final int MENSAGEM_CHAN_PRES    = 208;
        private static final int MENSAGEM_PITCH_BEND   = 224;

        private LeitorEventosMidi(Track[] trilhas) {
            if (trilhas != null) {
                this.trilhas = trilhas;
            } else {
                this.trilhas = new Track[0];
            }
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
            String msg_status;
            switch (status & 0xF0) {
                case MENSAGEM_NOTE_OFF:
                    msg_status = "Note off"; break;
                case MENSAGEM_NOTE_ON:
                    msg_status = "Note on"; break;
                case MENSAGEM_POLY_KEY:
                    msg_status = "Polyphonic key pressure"; break;
                case MENSAGEM_CTRL_CHANGE:
                    msg_status = "Control change"; break;
                case MENSAGEM_PROG_CHANGE:
                    msg_status = "Program change"; break;
                case MENSAGEM_CHAN_PRES:
                    msg_status = "Channel pressure"; break;
                case MENSAGEM_PITCH_BEND:
                    msg_status = "Pitch bend"; break;
                default:
                    msg_status = (status == 0xFF) ? "Meta mensagem" : Integer.toString(status);
            }
            String[] linha = new String[5];
            linha[0] = Integer.toString(indiceTrilhaAtual);
            linha[1] = Long.toString(evento.getTick());
            linha[2] = "C";
            linha[3] = Integer.toString(status);
            linha[4] = msg_status;
            return linha;
        }

        private void obtemDadosTabela() {
            dadosTabela = new String[totalLinhas][5];
            for (int i = 0; i < totalLinhas; ++i) {
                dadosTabela[i] = obtemProximaLinha();
            }
        }

        private void reiniciar() {
            indiceTrilhaAtual = 0;
            indiceEventoAtual = 0;
        }

    }

}
