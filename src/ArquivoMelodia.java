
import java.io.File;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class ArquivoMelodia 
{
    static int[] melodiax = { 60, 64, 67, 68, 70, 72};
    static int[] durax    = { 120, 240, 60, 60, 240, 360};
    static int[] intenx   = { 60, 80, 90, 100, 70, 50};

    static  Sequence   sequencia;
    static  int        tipoARQUIVO = 1;
    static  int        bpm = 60;
  
    public static void main(String args[]) 
    {     
      try
      { constroiSequenciaMidi();
        salvar("testex.mid");
      }
      catch(Exception e1){ System.out.println("*--ERRO na geração do arquivo MIDI: "+e1);}
    }


    static void constroiSequenciaMidi() throws Exception
    {
      int seminima = 120;   
      int resolucao = 24;

      sequencia = new Sequence(Sequence.PPQ, resolucao);

      Track[] trilha = new Track[2];

      double       tique1=0, tique2=0;
      int          canal    = 0;
      int          numerogm = 0; //--(piano)
      
      trilha[0] = sequencia.createTrack();

      //--- Dar valor ao andamento na trilha 0:  
      estabelecerAndamento(bpm, trilha[0]);

      double ktempo   = 1.0*(double)resolucao/(double)seminima;
      int itrilha     = 1;
      trilha[itrilha] = sequencia.createTrack();
         
      int operando1 = 0;
      int operando2 = 100;
      
      int t =0;               //---tempo relativo ao início da melodia
      

      ShortMessage mensagem;

      //----define o instrumento (program-change) uma única vez por voz.
      if(numerogm<128)
      {  mensagem = new ShortMessage();
         mensagem.setMessage(ShortMessage.PROGRAM_CHANGE, canal, numerogm, 0);
         trilha[itrilha].add(new MidiEvent(mensagem, 0));
      }

      
      //----------------------
      for(int j=0; j<melodiax.length; j++)
      {                               
	  tique1 =  t*ktempo;
	  tique2 = (t + durax[j])*ktempo;

	  operando1 = melodiax[j];                                    
	  operando2 = intenx[j];

	  mensagem = new ShortMessage();                           
	  mensagem.setMessage(ShortMessage.NOTE_ON, canal, operando1, operando2);
	  trilha[itrilha].add(new MidiEvent(mensagem, (int)(tique1)));

	  mensagem = new ShortMessage();
	  mensagem.setMessage(ShortMessage.NOTE_OFF, canal, operando1, 64);
	  trilha[itrilha].add(new MidiEvent(mensagem, (int)(tique2)));
		
	  t += durax[j];
	 }            
   } //--fim do método constroiSequenciaMidi()


    static void estabelecerAndamento(int bpm, Track trilha) throws InvalidMidiDataException 
    {
        MetaMessage mensagemDeAndamento = new MetaMessage();
        int microssegundos = (int)(60000000 / bpm);
        byte dados[] = new byte[3];
        dados[0] = (byte)(microssegundos >>> 16);
        dados[1] = (byte)(microssegundos >>> 8);
        dados[2] = (byte)(microssegundos);
        mensagemDeAndamento.setMessage(0x51, dados, 3);
        trilha.add(new MidiEvent(mensagemDeAndamento, 0));
    }


    static void estabelecerCompasso(int numerador, int denominador, Track trilha) throws InvalidMidiDataException 
    {
        MetaMessage mensagemDeCompasso = new MetaMessage();
        int denominadorExp = (int) (Math.log((double)denominador) / Math.log(2.0));
        byte dados[] = new byte[4];
        dados[0] = (byte)numerador;
        dados[1] = (byte)denominadorExp;
        dados[2] = (byte)24;
        dados[3] = (byte)8;
        mensagemDeCompasso.setMessage(0x58, dados, 4);
        trilha.add(new MidiEvent(mensagemDeCompasso, 0));
    }



    //--------------
    static void salvar(String midi_)
    { 
      String barra         = System.getProperty("file.separator");
      String diretorioMIDI = ".";
      String midiDirectoy = diretorioMIDI+barra+"mid";

      File file=new File(midiDirectoy);
      if(!file.exists())   //-- O diretório mid não existe. Então será criado.
           try{ boolean ok = (new File(midiDirectoy)).mkdir();
                if(ok) System.out.println("--> Criado o diretório:  " + midiDirectoy);
              }
           catch(Exception e){ System.err.println("--> Erro: " + e.getMessage()); }

      String arquivoMidi = diretorioMIDI+barra+"mid"+barra+midi_;
      try
      { MidiSystem.write(sequencia, tipoARQUIVO, new File(arquivoMidi));
        //System.out.println("--> Criado o arquivo Midi :  ["+arquivoMidi+"]");
      }
      catch(Exception e1){ System.out.println("--> Não foi possível salvar o arquivo MIDI: "+e1);};
    }

}
