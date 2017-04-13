import javax.sound.midi.MidiMessage;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ricardo on 09/04/2017.
 */

public class TelaEventosMidi extends JFrame {
    
    private JTable tabelaEventosMidi;
    private final Dimension tamanhoDaJanela = new Dimension(800, 600);
    private final ArrayList<MidiEventoTrilha> eventos;
    private final double durTique;
    private static final int MENSAGEM_NOTE_OFF     = 128;
    private static final int MENSAGEM_NOTE_ON      = 144;
    private static final int MENSAGEM_POLY_KEY     = 160;
    private static final int MENSAGEM_CTRL_CHANGE  = 176;
    private static final int MENSAGEM_PROG_CHANGE  = 192;
    private static final int MENSAGEM_CHAN_PRES    = 208;
    private static final int MENSAGEM_PITCH_BEND   = 224;

    public TelaEventosMidi(ArrayList<MidiEventoTrilha> eventos, double durTique) {
        super("Mensagens MIDI");
        
        this.eventos = eventos;
        this.durTique = durTique;
        
        configuraTelaEventos();
        configuraTabela();

        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void configuraTelaEventos() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(tamanhoDaJanela);
    }

    private void configuraTabela() {
        
        String[] colunasTabela = {"Trilha", "Instante (tiques)", "Instante (segundos)",
                                    "Op. 1", "Op. 2", "Op. 3", "Código", "Mensagem"};
        tabelaEventosMidi = new JTable(obtemDadosTabela(eventos), colunasTabela);
        tabelaEventosMidi.getColumnModel().getColumn(0).setMaxWidth(48);
        tabelaEventosMidi.getColumnModel().getColumn(1).setMaxWidth(100);
        tabelaEventosMidi.getColumnModel().getColumn(1).setMinWidth(100);
        tabelaEventosMidi.getColumnModel().getColumn(2).setMaxWidth(120);
        tabelaEventosMidi.getColumnModel().getColumn(2).setMinWidth(120);
        tabelaEventosMidi.getColumnModel().getColumn(3).setMaxWidth(44);
        tabelaEventosMidi.getColumnModel().getColumn(4).setMaxWidth(44);
        tabelaEventosMidi.getColumnModel().getColumn(5).setMaxWidth(44);
        tabelaEventosMidi.getColumnModel().getColumn(6).setMaxWidth(82);
        tabelaEventosMidi.getColumnModel().getColumn(6).setMinWidth(82);
        tabelaEventosMidi.setFillsViewportHeight(true);
        tabelaEventosMidi.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(tabelaEventosMidi));
    }

    private String[] processaEvento(MidiEventoTrilha evento) {
        MidiMessage mensagem = evento.evento.getMessage();
        byte bytes_mensagem[] = mensagem.getMessage();
        int status = mensagem.getStatus();
        String statusString = Integer.toString(status);
        boolean reportarEmQualCanal = false;
        StringBuilder sb = new StringBuilder();
        switch (status & 0xF0) {
            case MENSAGEM_NOTE_OFF:
                sb.append("[Note OFF] ").append(converteNota(bytes_mensagem[1]));
                reportarEmQualCanal = true;
                break;
            case MENSAGEM_NOTE_ON:
                sb.append("[Note ON] ").append(converteNota(bytes_mensagem[1]));
                reportarEmQualCanal = true;
                break;
            case MENSAGEM_POLY_KEY:
                sb.append("[Polyphonic key pressure]");
                break;
            case MENSAGEM_CTRL_CHANGE:
                sb.append("[Control change] - ");
                reportarEmQualCanal = true;
                switch (bytes_mensagem[1]) {
                    case 0x00: sb.append("Bank Select"); break;
                    case 0x01: sb.append("Modulation Wheel or Lever"); break;
                    case 0x02: sb.append("Breath Controller"); break;
                    case 0x03: sb.append("Undefined"); break;
                    case 0x04: sb.append("Foot Controller"); break;
                    case 0x05: sb.append("Portamento Time"); break;
                    case 0x06: sb.append("Data Entry MSB"); break;
                    case 0x07: sb.append("Channel Volume (formerly Main Volume)"); break;
                    case 0x08: sb.append("Balance"); break;
                    case 0x09: sb.append("Undefined"); break;
                    case 0x0A: sb.append("Pan"); break;
                    case 0x0B: sb.append("Expression Controller"); break;
                    case 0x0C: sb.append("Effect Control 1"); break;
                    case 0x0D: sb.append("Effect Control 2"); break;
                    case 0x10: sb.append("General Purpose Controller 1"); break;
                    case 0x11: sb.append("General Purpose Controller 2"); break;
                    case 0x12: sb.append("General Purpose Controller 3"); break;
                    case 0x13: sb.append("General Purpose Controller 4"); break;
                    case 0x20: sb.append("LSB for Control 0 (Bank Select)"); break;
                    case 0x21: sb.append("LSB for Control 1 (Modulation Wheel or Lever)"); break;
                    case 0x22: sb.append("LSB for Control 2 (Breath Controller)"); break;
                    case 0x23: sb.append("LSB for Control 3 (Undefined)"); break;
                    case 0x24: sb.append("LSB for Control 4 (Foot Controller)"); break;
                    case 0x25: sb.append("LSB for Control 5 (Portamento Time)"); break;
                    case 0x26: sb.append("LSB for Control 6 (Data Entry)"); break;
                    case 0x27: sb.append("LSB for Control 7 (Channel Volume, formerly Main Volume)"); break;
                    case 0x28: sb.append("LSB for Control 8 (Balance)"); break;
                    case 0x29: sb.append("LSB for Control 9 (Undefined)"); break;
                    case 0x2A: sb.append("LSB for Control 10 (Pan)"); break;
                    case 0x2B: sb.append("LSB for Control 11 (Expression Controller)"); break;
                    case 0x2C: sb.append("LSB for Control 12 (Effect control 1)"); break;
                    case 0x2D: sb.append("LSB for Control 13 (Effect control 2)"); break;
                    case 0x2E: sb.append("LSB for Control 14 (Undefined)"); break;
                    case 0x2F: sb.append("LSB for Control 15 (Undefined)"); break;
                    case 0x30: sb.append("LSB for Control 16 (General Purpose Controller 1)"); break;
                    case 0x31: sb.append("LSB for Control 17 (General Purpose Controller 2)"); break;
                    case 0x32: sb.append("LSB for Control 18 (General Purpose Controller 3)"); break;
                    case 0x33: sb.append("LSB for Control 19 (General Purpose Controller 4)"); break;
                    case 0x34: sb.append("LSB for Control 20 (Undefined)"); break;
                    case 0x35: sb.append("LSB for Control 21 (Undefined)"); break;
                    case 0x36: sb.append("LSB for Control 22 (Undefined)"); break;
                    case 0x37: sb.append("LSB for Control 23 (Undefined)"); break;
                    case 0x38: sb.append("LSB for Control 24 (Undefined)"); break;
                    case 0x39: sb.append("LSB for Control 25 (Undefined)"); break;
                    case 0x3A: sb.append("LSB for Control 26 (Undefined)"); break;
                    case 0x3B: sb.append("LSB for Control 27 (Undefined)"); break;
                    case 0x3C: sb.append("LSB for Control 28 (Undefined)"); break;
                    case 0x3D: sb.append("LSB for Control 29 (Undefined)"); break;
                    case 0x3E: sb.append("LSB for Control 30 (Undefined)"); break;
                    case 0x3F: sb.append("LSB for Control 31 (Undefined)"); break;
                    case 0x40: sb.append("Damper Pedal on/off (Sustain)"); break;
                    case 0x41: sb.append("Portamento On/Off"); break;
                    case 0x42: sb.append("Sostenuto On/Off"); break;
                    case 0x43: sb.append("Soft Pedal On/Off"); break;
                    case 0x44: sb.append("Legato Footswitch"); break;
                    case 0x45: sb.append("Hold 2"); break;
                    case 0x46: sb.append("Sound Controller 1 (default: Sound Variation)"); break;
                    case 0x47: sb.append("Sound Controller 2 (default: Timbre/Harmonic Intens.)"); break;
                    case 0x48: sb.append("Sound Controller 3 (default: Release Time)"); break;
                    case 0x49: sb.append("Sound Controller 4 (default: Attack Time)"); break;
                    case 0x4A: sb.append("Sound Controller 5 (default: Brightness)"); break;
                    case 0x4B: sb.append("Sound Controller 6 (default: Decay Time - see MMA RP-021)"); break;
                    case 0x4C: sb.append("Sound Controller 7 (default: Vibrato Rate - see MMA RP-021)"); break;
                    case 0x4D: sb.append("Sound Controller 8 (default: Vibrato Depth - see MMA RP-021)"); break;
                    case 0x4E: sb.append("Sound Controller 9 (default: Vibrato Delay - see MMA RP-021)"); break;
                    case 0x4F: sb.append("Sound Controller 10 (default undefined - see MMA RP-021)"); break;
                    case 0x50: sb.append("General Purpose Controller 5"); break;
                    case 0x51: sb.append("General Purpose Controller 6"); break;
                    case 0x52: sb.append("General Purpose Controller 7"); break;
                    case 0x53: sb.append("General Purpose Controller 8"); break;
                    case 0x54: sb.append("Portamento Control"); break;
                    case 0x58: sb.append("High Resolution Velocity Prefix"); break;
                    case 0x5B: sb.append("Effects 1 Depth (formerly External Effects Depth)"); break;
                    case 0x5C: sb.append("Effects 2 Depth (formerly Tremolo Depth)"); break;
                    case 0x5D: sb.append("Effects 3 Depth (formerly Chorus Depth)"); break;
                    case 0x5E: sb.append("Effects 4 Depth (formerly Celeste [Detune] Depth)"); break;
                    case 0x5F: sb.append("Effects 5 Depth (formerly Phaser Depth)"); break;
                    case 0x60: sb.append("Data Increment (Data Entry +1)"); break;
                    case 0x61: sb.append("Data Decrement (Data Entry -1)"); break;
                    case 0x62: sb.append("Non-Registered Parameter Number (NRPN) - LSB"); break;
                    case 0x63: sb.append("Non-Registered Parameter Number (NRPN) - MSB"); break;
                    case 0x64: sb.append("Registered Parameter Number (RPN) - LSB*"); break;
                    case 0x65: sb.append("Registered Parameter Number (RPN) - MSB*"); break;
                    case 0x78: sb.append("[Channel Mode Message] All Sound Off"); break;
                    case 0x79: sb.append("[Channel Mode Message] Reset All Controllers (See MMA RP-015)"); break;                                
                    case 0x7A: sb.append("[Channel Mode Message] Local Control On/Off"); break;                                
                    case 0x7B: sb.append("[Channel Mode Message] All Notes Off"); break;                                
                    case 0x7C: sb.append("[Channel Mode Message] Omni Mode Off (+ all notes off)"); break;                                
                    case 0x7D: sb.append("[Channel Mode Message] Omni Mode On (+ all notes off)"); break;                                
                    case 0x7E: sb.append("[Channel Mode Message] Mono Mode On (+ poly off, + all notes off)"); break;                                
                    case 0x7F: sb.append("[Channel Mode Message] Poly Mode On (+ mono off, +all notes off)"); break;                                
                    default: sb.append("Undefined"); break;
                }
                break;
            case MENSAGEM_PROG_CHANGE:
                sb.append("[Program change]");
                reportarEmQualCanal = true;
                break;
            case MENSAGEM_CHAN_PRES:
                sb.append("[Channel pressure]");
                reportarEmQualCanal = true;
                break;
            case MENSAGEM_PITCH_BEND:
                sb.append("[Pitch bend]");
                reportarEmQualCanal = true;
                break;
            default:
                sb.append((status == 0xFF) ? "[Meta mensagem]" : Integer.toString(status));
                break;
        }
        
        if (reportarEmQualCanal) {
            statusString = Integer.toString(status & 0xF0);
            statusString += ", canal: " + (status - (int)(bytes_mensagem[0] & 0xF0));
        }
        
        String[] linha = new String[8];
        linha[0] = Integer.toString(evento.trilhaId); // Trilha
        linha[1] = Long.toString(evento.evento.getTick()); // Instante (tiques)
        linha[2] = String.format("%.2f s", evento.evento.getTick() * durTique); // Instante (segundos)
        linha[3] = bytes_mensagem.length > 1 ? Byte.toString(bytes_mensagem[1]) : "-"; // Op. 1
        linha[4] = bytes_mensagem.length > 2 ? Byte.toString(bytes_mensagem[2]) : "-"; // Op. 2
        linha[5] = bytes_mensagem.length > 3 ? Byte.toString(bytes_mensagem[3]) : "-"; // Op. 3
        linha[6] = statusString; // Código
        linha[7] = sb.toString(); // Mensagem
        return linha;
    }

    private String[][] obtemDadosTabela(ArrayList<MidiEventoTrilha> eventos) {
        String[][] dadosTabela = new String[eventos.size()][5];
        for (int i = 0; i < eventos.size(); i++) {
            dadosTabela[i] = processaEvento(eventos.get(i));
        }
        return dadosTabela;
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
}
