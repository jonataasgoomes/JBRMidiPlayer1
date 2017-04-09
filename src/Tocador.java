import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public class Tocador {
    
    private class MidiEventoTrilha {
        
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
    
    private Soundbank bancoDeInstrumentos = null;
    private Synthesizer sintetizador = null;
    private Sequencer sequenciador = null;
    private Receiver receptor = null;
    private String problemaAoInstanciar = null;
    
    private List<MidiEvent> eventosMidiOriginais = new ArrayList<>();
    private List<MidiEventoTrilha> eventosMidiVolume = new ArrayList<>();
    private List<MidiEventoTrilha> eventosMidiBPM = new ArrayList<>();
    private List<MidiEvent> eventosMidiBPMRemoviveis = new ArrayList<>();
    private int volumeBase = -1;
    private int bpmBase = -1;
    
    private float volumeAtual = -1.0f;
    private int bpmAtual = -1;
    
    private double duracaoNormal;
    
    private int mudarBpmAoTocar = -1;
    
    public Tocador() {
        try {
            sintetizador = MidiSystem.getSynthesizer();
            sequenciador = MidiSystem.getSequencer(false);
            sintetizador.open();
            sequenciador.open();
            if (sequenciador.isOpen() && sintetizador.isOpen()) {
                bancoDeInstrumentos = sintetizador.getDefaultSoundbank();
                receptor = sintetizador.getReceiver();
                sequenciador.getTransmitter().setReceiver(receptor);
            } else {
                problemaAoInstanciar = "Falha ao abrir o sequenciador.";
                finalizar();
            }
        } catch (Exception ex) {
            problemaAoInstanciar = ex.toString();
        }
    }
    
    public double obtemDuracaoRealSegundos() {
        
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        if (bpmAtual == -1) {
            return sequencia.getMicrosecondLength() / 1000000.0d;
        }
        
        double fator = bpmBase / (double)bpmAtual;
        
        return obtemDuracaoNormalSegundos() * fator;
    }
    
    // Retorna a duração em segundos de um MIDI
    // desconsiderando a velocidade de andamento.
    // Um aúdio de 30 segundos de duração acelerado
    // em 2x possui duração normal de 30 segundos.
    public double obtemDuracaoNormalSegundos() {
        return duracaoNormal;
    }
    
    public long obtemResolucao() {
        
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        return sequencia.getResolution();
    }
    
    public long obtemTotalTiques() {
        
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        return sequencia.getTickLength();
    }
    
    public double obtemDuracaoTique() {
        
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        double durTique = sequencia.getMicrosecondLength() / 1000000.0;
        durTique /= sequencia.getTickLength();
        
        return durTique;
    }
    
    public double obtemDuracaoSeminima() {
        
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        double durTique = sequencia.getMicrosecondLength() / 1000000.0;
        durTique /= sequencia.getTickLength();
        
        return durTique * sequencia.getResolution();
    }
    
    public long obtemTotalSeminimas() {
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        return (long)(obtemDuracaoRealSegundos() / obtemDuracaoSeminima());
    }
    
    public String obtemProblemaAoInstanciar() {
        return problemaAoInstanciar;
    }
    
    public double obtemPosicaoSegundos() {
        if (sequenciador != null) {
            double tempo = sequenciador.getMicrosecondPosition() / 1000000.d;
            return tempo;
        }
        return -1.d;
    }
    
    public void setPosicaoMicrosegundos(long microsegundos) {
        if (sequenciador != null) {
            sequenciador.setMicrosecondPosition(microsegundos);
        }
    }
    
    public boolean carregaBancoDeInstrumentos(String caminho) {
        return carregaBancoDeInstrumentos(new File(caminho));
    }
    
    public boolean carregaBancoDeInstrumentos(File arquivo) {
        
        if (sintetizador == null) {
            return false;
        }
        
        Soundbank bancoAntigo = bancoDeInstrumentos;
        
        try {
            
            bancoDeInstrumentos = MidiSystem.getSoundbank(arquivo);
            
            if (bancoAntigo != null) {
                sintetizador.unloadAllInstruments(bancoAntigo);
            }
            
            sintetizador.loadAllInstruments(bancoDeInstrumentos);
            
        } catch (InvalidMidiDataException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    public boolean carregaArquivo(String caminho) {
        return carregaArquivo(new File(caminho));
    }
    
    public boolean carregaArquivo(File arquivo) {
        
        if (sequenciador == null)
            return false;
        
        try {
            
            parar();
            
            Sequence sequencia = MidiSystem.getSequence(arquivo);
            sequenciador.setSequence(sequencia);
            
            duracaoNormal = sequencia.getMicrosecondLength() / 1000000.0d;
            
            Track[] trilhas = sequencia.getTracks();
            bpmBase = -1;
            volumeBase = -1;

            eventosMidiOriginais.clear();
            eventosMidiVolume.clear();
            eventosMidiBPM.clear();
            
            for (int trilhaId = 0; trilhaId < trilhas.length; trilhaId++) {
                
                Track trilha = trilhas[trilhaId];
                
                for (int eventoId = 0; eventoId < trilha.size(); eventoId++) {
                    MidiEvent evento = trilha.get(eventoId);
                    MidiMessage msg = evento.getMessage();
                    byte[] bytes = msg.getMessage();
                    eventosMidiOriginais.add(evento);
                    int status = msg.getStatus();
                    if (status == 255 && bytes[1] == 0x51 && bytes[2] == 3) { // Meta Mensagem - Set Tempo
                        eventosMidiBPM.add(new MidiEventoTrilha(evento, trilhaId));
                        if (bpmBase == -1) {
                            int valorTempo = (int)(bytes[5] & 0xFF);
                            valorTempo += (int)(bytes[4] & 0xFF) * 256;
                            valorTempo += (int)(bytes[3] & 0xFF) * 65536;
                            bpmBase = 60000000 / valorTempo;
                        }
                    } else if (status >= 0xB0 && status <= 0xBF && bytes[1] == 0x07) { // Control Change - Volume
                        eventosMidiVolume.add(new MidiEventoTrilha(evento, trilhaId));
                        if (volumeBase == -1) {
                            volumeBase = (int)(bytes[2] & 0xFF);
                        }
                    } else if (status >= 0x90 && status <= 0x9F) { // Note On
                        if (volumeBase == -1) {
                            volumeBase = 50;
                        }
                        if (bpmBase == -1) {
                            bpmBase = 100;
                        }
                    }
                }
            }
            
            for (int trilhaId = 0; trilhaId < trilhas.length; trilhaId++) {
                
                Track trilha = trilhas[trilhaId];
                
                for (int canalId = 15; canalId >= 0; canalId--) {
                    MidiEvent evento = new MidiEvent(
                        (MidiMessage)new ShortMessage(
                            ShortMessage.CONTROL_CHANGE, canalId, 7,
                                volumeBase
                            ), 0);
                    eventosMidiVolume.add(0, new MidiEventoTrilha(evento, trilhaId));
                    trilha.add(evento);
                }
            }
            
            int microSegundos = 60000000 / bpmBase;
                
            byte[] bytes = new byte[] {
                (byte)(microSegundos >> 16),
                (byte)(microSegundos >> 8),
                (byte)(microSegundos),
            };
            
            if (volumeAtual != -1.0f) {
                controlaVolume(volumeAtual);
            }
            
            if (bpmAtual > -1) {
                controlaAndamento(bpmAtual);
            }
            
        } catch (Exception ex) {
            return false;
        }
        
        return true;
    }
    
    public void controlaAndamento(int bpm) {
        
        if (sequenciador == null || bpm <= 0)
            return;
        
        Sequence sequencia = sequenciador.getSequence();
        
        if (sequencia == null)
            return;
        
        if (!sequenciador.isRunning()) {
            mudarBpmAoTocar = bpm;
            return;
        }
        
        int microSegundos = 60000000 / bpm;
        
        byte[] bytes = new byte[3];

        bytes[0] = (byte)(microSegundos >>> 16);
        bytes[1] = (byte)(microSegundos >>> 8);
        bytes[2] = (byte) microSegundos;
        
        Track[] trilhas = sequencia.getTracks();
        float fator = (float)bpm / bpmBase;
        
        bpmAtual = bpm;
        
        try {
            
            long tiqueAtual = sequenciador.getTickPosition();
            int microSegundosNoMomento = 600000;
            
            for (MidiEventoTrilha evento : eventosMidiBPM) {
                
                if (evento.evento.getTick() < tiqueAtual) {
                    microSegundosNoMomento = evento.valor;
                }
                
                microSegundos = evento.valor;
                microSegundos = (int)(microSegundos / fator);
                bytes = evento.evento.getMessage().getMessage();
                bytes[0] = (byte)(microSegundos >>> 16);
                bytes[1] = (byte)(microSegundos >>> 8);
                bytes[2] = (byte) microSegundos;

                trilhas[evento.trilhaId].remove(evento.evento);
                MidiMessage msg = new MetaMessage(0x51, bytes, 3);
                MidiEvent novoEvento = new MidiEvent(msg, evento.evento.getTick());
                trilhas[evento.trilhaId].add(novoEvento);
                evento.evento = novoEvento;
            }

            microSegundosNoMomento /= fator;
            bytes[0] = (byte)(microSegundosNoMomento >>> 16);
            bytes[1] = (byte)(microSegundosNoMomento >>> 8);
            bytes[2] = (byte) microSegundosNoMomento;
            
            MidiEvent eventoControladorAndamento = 
                new MidiEvent(new MetaMessage(
                    0x51, bytes, 3
                ), sequenciador.getTickPosition() + 1)
            ;
            
            trilhas[0].add(eventoControladorAndamento);
            eventosMidiBPMRemoviveis.add(eventoControladorAndamento);
            
//            receptor.send(new MetaMessage(
//                    0x51, bytes, 3
//            ), -1);
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void controlaVolume(float fator) {
        
        if (sequenciador == null || fator == -1.0f)
            return;
        
        Sequence sequencia = sequenciador.getSequence();
        
        if (sequencia == null)
            return;
        
        if (fator < 0.f) {
            fator = 0.f;
        } else if (fator > 1.f) {
            fator = 1.f;
        }
        
        volumeAtual = fator;
        
        Track[] trilhas = sequencia.getTracks();
        
        try {
            
            long tiqueAtual = sequenciador.getTickPosition();
            int volumeNoMomento = 50;
            
            for (MidiEventoTrilha evento : eventosMidiVolume) {
                
                if (evento.evento.getTick() < tiqueAtual) {
                    volumeNoMomento = evento.valor;
                }
                
                Track trilha = trilhas[evento.trilhaId];
                int novoValor = (int)(fator * evento.valor);
                ShortMessage msg = new ShortMessage(
                    ShortMessage.CONTROL_CHANGE, evento.canal, 7, novoValor
                );
                MidiEvent novoEvento = new MidiEvent(msg, evento.evento.getTick());
                trilha.remove(evento.evento);
                evento.evento = novoEvento;
                trilha.add(novoEvento);
                
            }
            
            volumeNoMomento *= fator;
            
            if (volumeNoMomento > 127) volumeNoMomento = 127;
            if (volumeNoMomento < 0) volumeNoMomento = 0;
            
            for (int canal = 0; canal < 16; canal++) {
                receptor.send(new ShortMessage(
                        ShortMessage.CONTROL_CHANGE, canal, 7, volumeNoMomento
                ), -1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void imprimeEventosMidi() {
        
        if (sequenciador == null)
            return;
        
        Sequence sequencia = sequenciador.getSequence();
        
        if (sequencia == null) {
            return;
        }
        
        int trilhaId = 0;
        for (Track trilha : sequencia.getTracks()) {
            int numeroDeEventos = trilha.size();
            for (int eventoId = 0; eventoId < numeroDeEventos; eventoId++) {
                MidiEvent evento = trilha.get(eventoId);
                MidiMessage msg = evento.getMessage();
                String str = "T:" + trilhaId + " - E:" + eventoId + " S:" + msg.getStatus() + " " + evento.getTick();
                if (msg.getLength() > 0) {
                    for (int i = 0; i < msg.getLength(); i++)
                        str += " > " + (int)(msg.getMessage()[i] & 0xff);
                }
                System.out.println(str);
            }
            trilhaId++;
        }
        
    }
    
    public void tocar() {
        if (sequenciador != null && !sequenciador.isRunning()) {
            
            sequenciador.start();
            
            if (mudarBpmAoTocar != -1) {
                controlaAndamento(mudarBpmAoTocar);
                mudarBpmAoTocar = -1;
            }
        }
    }
    
    public void pausar() {
        if (sequenciador != null && sequenciador.isRunning()) {
            sequenciador.stop();
        }
    }
    
    public void parar() {
        if (sequenciador != null) {
            
            Sequence sequencia = sequenciador.getSequence();
            
            if (sequencia == null) {
                return;
            }
            
            int bpmAoRetocar = bpmAtual;
            
            controlaAndamento(bpmBase);
            
            sequenciador.stop();
            sequenciador.setTickPosition(0);
            
            controlaAndamento(bpmAoRetocar);
            
            Track[] trilhas = sequencia.getTracks();
            
            for (MidiEvent evento : eventosMidiBPMRemoviveis) {
                trilhas[0].remove(evento);
            }
            
            eventosMidiBPMRemoviveis.clear();
            
        }
    }
    
    public final void finalizar() {
        if (sequenciador != null && sequenciador.isOpen()) {
            sequenciador.close();
        }
        
        if (sintetizador != null && sintetizador.isOpen()) {
            sintetizador.close();
        }
    }
    
}
