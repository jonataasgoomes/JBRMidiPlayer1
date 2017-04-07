package tocadorics;

import java.io.File;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class Tocador {
    
    private Soundbank bancoDeInstrumentos = null;
    private Synthesizer sintetizador = null;
    private Sequencer sequenciador = null;
    private Receiver receptor = null;
    private String problemaAoInstanciar = null;
    private int volumeAtual = -1;
    
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
    
    public long obtemDuracaoSegundos() {
        
        if (sequenciador == null) {
            return -1;
        }
            
        Sequence sequencia = sequenciador.getSequence();
            
        if (sequencia == null) {
            return 0;
        }
        
        return sequencia.getMicrosecondLength() / 1000000;
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
        
        return (long)(obtemDuracaoSegundos() / obtemDuracaoSeminima());
    }
    
    public String obtemProblemaAoInstanciar() {
        return problemaAoInstanciar;
    }
    
    public long obtemPosicaoMicrosegundos() {
        if (sequenciador != null) {
            return sequenciador.getMicrosecondPosition();
        }
        return -1;
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
            
        } catch (Exception ex) {
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
            sequenciador.setSequence(MidiSystem.getSequence(arquivo));
            
            if (volumeAtual > -1) {
                controlaVolume(volumeAtual);
            }
            
        } catch (Exception ex) {
            return false;
        }
        
        return true;
    }
    
    public void controlaVolume(int valor) {
        
        if (receptor == null)
            return;
        
        volumeAtual = valor;
        
        try {
            for(int canal = 0; canal < 16; canal++)
            {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.CONTROL_CHANGE, canal, 7, valor);
                receptor.send(msg, -1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("controla volume: " + valor);
        
    }
    
    public void tocar() {
        if (sequenciador != null) {
            sequenciador.start();
            System.out.println("tocar");
        }
    }
    
    public void pausar() {
        if (sequenciador != null && sequenciador.isRunning()) {
            sequenciador.stop();
            System.out.println("pausar");
        }
    }
    
    public void parar() {
        if (sequenciador != null && sequenciador.isRunning()) {
            sequenciador.stop();
            sequenciador.setTickPosition(0);
            System.out.println("parar");
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
