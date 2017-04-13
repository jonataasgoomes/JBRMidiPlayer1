
import javax.sound.midi.MidiEvent;

public class MidiEventoTrilha {
        
        public MidiEvent evento;
        public int trilhaId;
        public int canal;
        public int valor;
        
        MidiEventoTrilha (MidiEvent evento, int trilhaId) {
            
            this.trilhaId = trilhaId;
            this.evento = evento;
            
            byte[] bytes = evento.getMessage().getMessage();
            int length = evento.getMessage().getLength();
            int status = evento.getMessage().getStatus();
            
            if (length == 6 && status == 0xFF && bytes[1] == 0x51) { // Meta Mensagem - SetTempo
                valor = (int)(bytes[3] & 0xFF) * 65536;
                valor += (int)(bytes[4] & 0xFF) * 256;
                valor += (int)(bytes[5] & 0xFF);
            } else if (length == 3 && status >= 0xB0 &&
                       status <= 0xBF && bytes[1] == 0x07) { // Control Change - Volume
                canal = status - 0xB0;
                valor = (int)(bytes[2] & 0xFF);
            }
            
        }
    }