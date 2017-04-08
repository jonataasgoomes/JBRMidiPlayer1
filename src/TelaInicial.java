import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Ricardo on 07/04/2017.
 */

public class TelaInicial extends JFrame {

    public File arquivoMidi[];
    public File arquivoSoundfont[];
    public Tocador tocador;

    /* Controle de fluxo */
    private JButton btnPausa;
    private JButton btnTocar;
    private JButton btnParar;
    /* Progresso */
    private JProgressBar pbProgresso;
    private JLabel lbProgresso;
    /* Volume */
    private JSlider slVolume;
    private JLabel lbVolume;
    /* Seletor de arquivo MIDI */
    private JTextField tfNomeArquivo;
    private JButton btnCarregarArquivo;
    /* Seletor de soundfont */
    private JTextField tfNomeSoundfont;
    private JButton btnCarregarSoundfont;
    /* Informacoes gerais */
    private JTextArea taInformacoes;

    public TelaInicial() {
        super();

        tocador = new Tocador();
        arquivoMidi = new File[1];
        arquivoSoundfont = new File[1];

        setBounds(100, 100, 400, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(null);

        configuraControleDeFluxo();
        configuraProgresso();
        configuraVolume();
        configuraSeletorMidi();
        configuraSeletorSoundfont();
        configuraInformacoes();

        setVisible(true);
    }

    private void configuraControleDeFluxo() {
        btnPausa = new JButton("Pausar");
        btnTocar = new JButton("Reproduzir");
        btnParar = new JButton("Parar");

        btnPausa.setBounds(10, 527, 89, 23);
        btnTocar.setBounds(109, 527, 166, 23);
        btnParar.setBounds(285, 527, 89, 23);

        getContentPane().add(btnPausa);
        getContentPane().add(btnTocar);
        getContentPane().add(btnParar);
    }

    private void configuraProgresso() {
        pbProgresso = new JProgressBar();
        pbProgresso.setBounds(33, 470, 301, 14);
        lbProgresso = new JLabel("hh:mm:ss");
        lbProgresso.setBounds(109, 495, 166, 14);

        getContentPane().add(pbProgresso);
        getContentPane().add(lbProgresso);
    }

    private void configuraVolume() {
        slVolume = new JSlider();
        slVolume.setOrientation(SwingConstants.VERTICAL);
        slVolume.setBounds(344, 135, 30, 272);
        slVolume.setMaximum(127);
        slVolume.setMinimum(0);
        slVolume.setMajorTickSpacing(10);
        slVolume.setMinorTickSpacing(1);
        slVolume.setValue(64);
        slVolume.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int volume = (int) Math.round((slVolume.getValue() * 100.0) / 127.0);
                lbVolume.setText(volume + "%");
            }
        });

        lbVolume = new JLabel("50%");
        lbVolume.setBounds(344, 418, 30, 41);

        getContentPane().add(slVolume);
        getContentPane().add(lbVolume);
    }

    private void configuraSeletorMidi() {
        tfNomeArquivo = new JTextField();
        tfNomeArquivo.setEditable(false);
        tfNomeArquivo.setBounds(10, 11, 290, 20);
        tfNomeArquivo.setText("Arquivo MIDI");
        btnCarregarArquivo = new JButton("...");
        btnCarregarArquivo.setBounds(310, 10, 64, 21);
        btnCarregarArquivo.setToolTipText("Carregar arquivo MIDI");
        btnCarregarArquivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String extensoes[] = new String[2];
                extensoes[0] = ".mid";
                extensoes[1] = ".midi";
                abrirArquivo(".", extensoes, "Arquivos MIDI (*.mid, *.midi)", arquivoMidi);
                if (arquivoMidi[0] != null) {
                    if (tocador.carregaArquivo(arquivoMidi[0])) {
                        tfNomeArquivo.setText(arquivoMidi[0].toString());
                        atualizaInformacoes();
                    } else {
                        JOptionPane.showMessageDialog(null, "Falha no arquivo MIDI.");
                        arquivoMidi[0] = null;
                    }
                }
            }
        });
        getContentPane().add(tfNomeArquivo);
        getContentPane().add(btnCarregarArquivo);
    }

    private void configuraSeletorSoundfont() {
        tfNomeSoundfont = new JTextField();
        tfNomeSoundfont.setEditable(false);
        tfNomeSoundfont.setBounds(10, 41, 290, 20);
        tfNomeSoundfont.setText("Arquivo SoundFont");
        btnCarregarSoundfont = new JButton("...");
        btnCarregarSoundfont.setBounds(310, 40, 64, 21);
        btnCarregarSoundfont.setToolTipText("Carregar arquivo SoundFont");
        btnCarregarSoundfont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String extensoes[] = new String[1];
                extensoes[0] = ".sf2";
                abrirArquivo(".", extensoes, "Arquivos soundfont(*.sf2)", arquivoSoundfont);
                if (arquivoSoundfont[0] != null)
                    tfNomeSoundfont.setText(arquivoSoundfont[0].toString());
            }
        });

        getContentPane().add(tfNomeSoundfont);
        getContentPane().add(btnCarregarSoundfont);
    }

    private void configuraInformacoes() {
        taInformacoes = new JTextArea();
        taInformacoes.setBounds(33, 135, 301, 272);
        taInformacoes.setEditable(false);

        getContentPane().add(taInformacoes);
    }
    
    private void abrirArquivo(String caminho, String extensoes[], String descricao, File arquivo[]) {
        JFileChooser seletor = new JFileChooser(caminho);
        seletor.setFileSelectionMode(JFileChooser.FILES_ONLY);
        seletor.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                String name = f.getName().toLowerCase();
                for (String ext : extensoes) {
                    if (name.endsWith(ext))
                        return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return descricao;
            }
        });
        seletor.showOpenDialog(this);
        arquivo[0] = seletor.getSelectedFile();
    }

    public void atualizaInformacoes() {
        StringBuilder sb = new StringBuilder();
        long duracao = tocador.obtemDuracaoSegundos();
        long resolucao = tocador.obtemResolucao();
        double duracao_seminima = tocador.obtemDuracaoSeminima();
        long total_tiques = tocador.obtemTotalTiques();
        double duracao_tique = tocador.obtemDuracaoTique();
        double bpm = 60.0 / duracao_seminima;
        long total_seminimas = tocador.obtemTotalSeminimas();
        sb.append("Nome do arquivo: ").append(arquivoMidi[0].getName())
                .append("\nResolução: ").append(resolucao).append(" tiques por semínima")
                .append("\nDuração: ").append(divideTempo(duracao))
                .append("\nTotal de tiques: ").append(total_tiques)
                .append("\nDuração de tique: ").append(duracao_tique).append(" s")
                .append("\nDuração da semínima: ").append(duracao_seminima).append(" s")
                .append("\nNúmero de semínimas: ").append(total_seminimas)
                .append(String.format("\nAndamento: %.2f bpm", bpm));
        taInformacoes.setText(sb.toString());
    }

    public String divideTempo(long segundos) {
        byte horas = (byte) (segundos / 3600);
        segundos -= horas * 3600;
        byte minutos = (byte) (segundos / 60);
        segundos -= minutos * 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    public void reproduzOuPausa(boolean reproduzindo) {
        btnPausa.setEnabled(reproduzindo);
        btnTocar.setEnabled(!reproduzindo);
    }


}
