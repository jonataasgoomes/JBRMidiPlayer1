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

    private Soundbank bancoDeInstrumentos = null;
    private Synthesizer sintetizador = null;
    private Sequencer sequenciador = null;
    private Receiver receptor = null;
    // String que guarda qualquer falha ao instanciar a classe
    // Tocador. Esse valor deve ser verificado antes de utilizar
    // outros métodos da classe, pois se a classe não foi
    // inicializada corretamente, seu funcionamento não pode
    // ser garantido.
    private String problemaAoInstanciar = null;
    
    private List<MidiEvent> eventosMidiOriginais = new ArrayList<>();
    private List<MidiEventoTrilha> eventosMidiVolume = new ArrayList<>();
    private List<MidiEventoTrilha> eventosMidiBPM = new ArrayList<>();
    private List<MidiEvent> eventosMidiBPMRemoviveis = new ArrayList<>();
    
    // Valores iniciais de volume/BPM de uma música.
    // Quando uma música é carregada, o primeiro evento Midi
    // de mudança de volume define o volume "base" da música
    // e o primeiro evento Midi de mudança de BPM define
    // o BPM "base" da música.
    private int volumeBase = -1;
    private int bpmBase = -1;
    
    // Valores que podem ser modificados pelo usuário e
    // são constantes ao Tocador.
    // Valor do volume (de 0 até 1).
    private float volumeAtual = -1.0f;
    // Valor da velocidade de "playback". Um valor "X" maior do que 1
    // faz a música ser acelerada. Um valor menor do que 1 desacelera
    // a música.
    private float velocidadeAtual = -1; 
    
    // Quantos segundos a música dura. Esse valor é alterado
    // sempre que uma sequência (música) novoa é carregada.
    // Esse valor é guardado, pois ao alterar o andamento (BPM)
    // da música, a sua duração é pode se tornar menor ou maior.
    // Esse campo guarda a duração original da música.
    private double duracaoNormal;
    
    // Esse valor guarda mudanças feitas à velocidade enquanto
    // a música está pausada. A alteração de velocidade enquanto
    // a música está pausada causa comportamentos imprevisíveis,
    // porém ao dar "play" na música, a velocidade pode ser alterado de
    // acordo com o que se espera. Qualquer alteração na velocidade
    // enquanto a música está pausada é salvada nessa variável,
    // e quando a música for tocada novamente, sua velocidade é atualizado.
    private float mudarVelocidadeAoTocar = -1;
    
    public Tocador() {
        try {
            // Tenta inicializar o sintetizador e o sequenciador.
            sintetizador = MidiSystem.getSynthesizer();
            sequenciador = MidiSystem.getSequencer(false);
            sintetizador.open();
            sequenciador.open();
            if (sequenciador.isOpen() && sintetizador.isOpen()) {
                // Se os dois inicializarem corretamente, abre o banco
                // padrão de instrumentos, e inicializa o receptor.
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
    
    // Retorna a duração em segundos de um MIDI
    // desconsiderando a velocidade de andamento.
    // Um aúdio de 30 segundos de duração acelerado
    // em 2x possui duração normal de 30 segundos.
    public double obtemDuracaoNormalSegundos() {
        return duracaoNormal;
    }
    
    // Retorna a duração em segundos de um MIDI
    // considerando a velocidade de andamento.
    // Um aúdio de 30 segundos de duração acelerado
    // em 2x possui duração real de 15 segundos.
    public double obtemDuracaoRealSegundos() {

        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        if (velocidadeAtual == -1) {
            return sequencia.getMicrosecondLength() / 1000000.0d;
        }
        
        return obtemDuracaoNormalSegundos() / velocidadeAtual;
    }
    
    // Retorna a resolução da música carregada.
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
    
    // Retorna o total de tiques da música carregada.
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
    
    // Retorna a duração de cada tique (em segundos).
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
    
    // Retorna a duração de uma seminima em segundos.
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

    public int obtemAndamento() {
        double duracaoSeminima = obtemDuracaoSeminima();
        if (duracaoSeminima > 0) {
            return (int) Math.round(60 / duracaoSeminima);
        }
        return -1;
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
    
    public int obtemBPMBase() {
        return bpmBase;
    }
    
    // Retorna problemas que podem ter ocorrido durante
    // a inicialização da classe.
    public String obtemProblemaAoInstanciar() {
        return problemaAoInstanciar;
    }
    
    public double obtemPosicaoSegundos() {
        if (sequenciador != null) {
            double pct = (double)sequenciador.getTickPosition() / sequenciador.getTickLength();
            return pct * obtemDuracaoNormalSegundos();
        }
        return -1.d;
    }
    
    public void setPosicaoMicrosegundos(long microsegundos) {
        if (sequenciador != null) {
            double pct = obtemDuracaoNormalSegundos() * 1000000.;
            pct = (double)microsegundos / pct;
            long tique = (long)(sequenciador.getTickLength() * pct);
            if (tique < 0) {
                tique = 0;
            } else if (tique > sequenciador.getTickLength()) {
                tique = sequenciador.getTickLength();
            }
            sequenciador.setTickPosition(tique);
        }
    }
    
    public boolean acabou() {
        return sequenciador != null && sequenciador.getTickLength() == sequenciador.getTickPosition();
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
            
            long tiquePrimeiroNoteOn = Long.MAX_VALUE;
            long tiquePrimeiroVolumeChange = Long.MAX_VALUE;
            long tiquePrimeiroBPMChange = Long.MAX_VALUE;
            
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
                        if (evento.getTick() < tiquePrimeiroBPMChange) {
                            int valorTempo = (int)(bytes[5] & 0xFF);
                            valorTempo += (int)(bytes[4] & 0xFF) * 256;
                            valorTempo += (int)(bytes[3] & 0xFF) * 65536;
                            bpmBase = 60000000 / valorTempo;
                            tiquePrimeiroBPMChange = evento.getTick();
                        }
                    } else if (status >= 0xB0 && status <= 0xBF && bytes[1] == 0x07) { // Control Change - Volume
                        eventosMidiVolume.add(new MidiEventoTrilha(evento, trilhaId));
                        if (evento.getTick() < tiquePrimeiroVolumeChange) {
                            volumeBase = (int)(bytes[2] & 0xFF);
                            tiquePrimeiroVolumeChange = evento.getTick();
                        }
                    } else if (status >= 0x90 && status <= 0x9F) { // Note On
                        if (evento.getTick() < tiquePrimeiroNoteOn) {
                            tiquePrimeiroNoteOn = evento.getTick();
                        }
                    }
                }
            }
            
            if (tiquePrimeiroNoteOn < tiquePrimeiroVolumeChange) {
                volumeBase = 100;
            }
            
            if (tiquePrimeiroNoteOn < tiquePrimeiroBPMChange) {
                bpmBase = 50;
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
            
            
            if (volumeAtual != -1.0f) {
                controlaVolume(volumeAtual);
            }
            
            if (velocidadeAtual > -1) {
                controlaAndamento(velocidadeAtual);
            }
            
        } catch (Exception ex) {
            return false;
        }
        
        return true;
    }
    
    public void controlaAndamento(float velocidade) {
        
        if (sequenciador == null)
            return;
        
        velocidadeAtual = velocidade;
        
        if (!sequenciador.isRunning()) {
            mudarVelocidadeAoTocar = velocidade;
            return;
        }
        
        Sequence sequencia = sequenciador.getSequence();
        
        if (sequencia == null)
            return;
        
        byte[] bytes;
        int microSegundos;
        Track[] trilhas = sequencia.getTracks();
        
        try {
            
            long tiqueAtual = sequenciador.getTickPosition();
            int microSegundosNoMomento = 600000;
            
            for (MidiEventoTrilha evento : eventosMidiBPM) {
                
                if (evento.evento.getTick() < tiqueAtual) {
                    microSegundosNoMomento = evento.valor;
                }
                
                microSegundos = evento.valor;
                microSegundos = (int)(microSegundos / velocidade);
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

            microSegundosNoMomento /= velocidade;
            bytes = new byte[3];
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
            
            if (mudarVelocidadeAoTocar != -1) {
                controlaAndamento(mudarVelocidadeAoTocar);
                mudarVelocidadeAoTocar = -1;
            }
        }
    }
    
    public void pausar() {
        if (sequenciador != null && sequenciador.isRunning()) {
            long tiqueAtual = sequenciador.getTickPosition();
            float velocidadeAoRetocar = velocidadeAtual;
            controlaAndamento(1.f);
            sequenciador.stop();
            sequenciador.setTickPosition(tiqueAtual);
            controlaAndamento(velocidadeAoRetocar);
        }
    }
    
    public void parar() {
        if (sequenciador != null) {
            
            Sequence sequencia = sequenciador.getSequence();
            
            if (sequencia == null) {
                return;
            }
            
            float velocidadeAoRetocar = velocidadeAtual;
            
            controlaAndamento(1.f);
            
            sequenciador.stop();
            sequenciador.setTickPosition(0);
            
            controlaAndamento(velocidadeAoRetocar);
            
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

    public float getVelocidadeAtual() {
        return velocidadeAtual > 0 ? velocidadeAtual : 1.0f;
    }

    public Sequence getSequencia() {
        if (sequenciador != null) {
            return sequenciador.getSequence();
        }
        return null;
    }

}
