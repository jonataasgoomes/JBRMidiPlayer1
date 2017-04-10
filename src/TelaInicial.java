import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.plaf.metal.MetalSliderUI;

/**
 * Created by Ricardo on 07/04/2017.
 */

public class TelaInicial extends JFrame {

    public File arquivoMidi = null;
    public File arquivoSoundfont = null;
    public Tocador tocador = null;

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
    /*EVENTOS*/
    private JButton btnEventos;
    /* BPM*/
    private JLabel lbBpm;
    private JButton btnAumentarBpm;
    private JButton btnDiminuirBpm;
    /* Seletor de arquivo MIDI */
    private JTextField tfNomeArquivo;
    private JButton btnCarregarArquivo;
    /* Seletor de soundfont */
    private JTextField tfNomeSoundfont;
    private JButton btnCarregarSoundfont;
    /* Informacoes gerais */
    private JTextArea taInformacoes;
    /* Time para atualizao da barra de progresso */
    Timer rastreadorDeProgresso;

    public TelaInicial() {
        
        super();

        tocador = new Tocador();

        setBounds(100, 100, 400, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("Tocador JBR");
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);
        
        ImageIcon icon = new ImageIcon("./icon.png");
        setIconImage(icon.getImage());

        configuraControleDeFluxo();
        eventosMid();
        configuraBpm();
        configuraProgresso();
        configuraVolume();
        configuraSeletorMidi();
        configuraSeletorSoundfont();
        configuraInformacoes();

        rastreadorDeProgresso = new Timer(25, (ActionEvent e) -> {
            long posicaoSegundos = (long)tocador.obtemPosicaoSegundos();
            if (posicaoSegundos != -1) {
                pbProgresso.setValue((int) posicaoSegundos);
                lbProgresso.setText(divideTempo(posicaoSegundos));
            }
        });
        
        rastreadorDeProgresso.setRepeats(true);

        setVisible(true);
    }



    private void eventosMid(){

        btnEventos = new JButton("NOME");
        btnEventos.setBounds(285, 72, 89, 23);
        getContentPane().add(btnEventos);

    }

    private void configuraBpm(){

        lbBpm = new JLabel("BPM");
        lbBpm.setBounds(33, 418, 66, 41);

        btnAumentarBpm = new JButton("+");
        btnAumentarBpm.setBounds(105, 418, 62, 41);

        btnDiminuirBpm = new JButton("-");
        btnDiminuirBpm.setBounds(177, 418, 62, 41);

        getContentPane().add(btnAumentarBpm);
        getContentPane().add(btnDiminuirBpm);
        getContentPane().add(lbBpm);

    }

    private void configuraControleDeFluxo() {
        btnPausa = new JButton("Pausar");
        btnPausa.addActionListener(e -> {
            tocador.pausar();
            rastreadorDeProgresso.stop();
            reproduzOuPausa(false);
        });
        btnPausa.setEnabled(false);
        btnTocar = new JButton("Reproduzir");
        btnTocar.addActionListener(e -> {
            tocador.tocar();
            reproduzOuPausa(true);
            rastreadorDeProgresso.start();
        });
        btnTocar.setEnabled(false);
        btnParar = new JButton("Parar");
        btnParar.addActionListener(e -> {
            tocador.parar();
            rastreadorDeProgresso.stop();
            btnParar.setEnabled(false);
            btnPausa.setEnabled(false);
            btnTocar.setEnabled(true);
            atualizaProgresso();
        });
        btnParar.setEnabled(false);

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
        pbProgresso.setMinimum(0);
        pbProgresso.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                long posMicrossegundos = e.getX() * pbProgresso.getMaximum() / pbProgresso.getWidth() * 1000000;
                tocador.setPosicaoMicrosegundos(posMicrossegundos);
                atualizaProgresso();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        lbProgresso = new JLabel("hh:mm:ss");
        lbProgresso.setBounds(168, 485, 65, 14);

        getContentPane().add(pbProgresso);
        getContentPane().add(lbProgresso);
    }

    private void configuraVolume() {
        slVolume = new JSlider();
        slVolume.setOrientation(SwingConstants.VERTICAL);
        slVolume.setBounds(344, 135, 30, 272);
        slVolume.setMaximum(256);
        slVolume.setMinimum(0);
        slVolume.setMajorTickSpacing(10);
        slVolume.setMinorTickSpacing(1);
        slVolume.setValue(256);

        slVolume.addChangeListener((ChangeEvent e) -> {
            float pct = (float)slVolume.getValue() / slVolume.getMaximum();
            mudaVolume(pct);
        });
        
        slVolume.setUI(new MetalSliderUI() {
            @Override
            protected void scrollDueToClickInTrack(int direction) {
                int value = slider.getValue();
                if (slider.getOrientation() == JSlider.HORIZONTAL) {
                    value = this.valueForXPosition(slider.getMousePosition().x);
                } else if (slider.getOrientation() == JSlider.VERTICAL) {
                    value = this.valueForYPosition(slider.getMousePosition().y);
                }
                slider.setValue(value);
            }
        });
        
        lbVolume = new JLabel("");
        lbVolume.setBounds(334, 400, 62, 41);

        getContentPane().add(slVolume);
        getContentPane().add(lbVolume);
        
        mudaVolume(1.f);
    }
    
    private void mudaVolume(float valor) {
        int volume = (int)Math.round(valor * 100.0f);
        lbVolume.setText("Vol: " + volume + "%");
        tocador.controlaVolume(valor);
    }

    private void configuraSeletorMidi() {
        tfNomeArquivo = new JTextField();
        tfNomeArquivo.setEditable(false);
        tfNomeArquivo.setBounds(10, 11, 262, 20);
        tfNomeArquivo.setText("Arquivo MIDI");
        btnCarregarArquivo = new JButton("...");
        btnCarregarArquivo.setBounds(285, 10, 89, 21);
        btnCarregarArquivo.setToolTipText("Carregar arquivo MIDI");
        btnCarregarArquivo.addActionListener(e -> {
            String extensoes[] = new String[2];
            extensoes[0] = ".mid";
            extensoes[1] = ".midi";
            arquivoMidi = abrirArquivo("./midi", extensoes, "Arquivos MIDI (*.mid, *.midi)");
            if (arquivoMidi != null) {
                if (tocador.carregaArquivo(arquivoMidi)) {
                    tfNomeArquivo.setText(arquivoMidi.toString());
                    btnParar.setEnabled(false);
                    btnPausa.setEnabled(false);
                    btnTocar.setEnabled(true);
                    atualizaInformacoes();
                    pbProgresso.setMaximum((int)tocador.obtemDuracaoNormalSegundos());
                    atualizaProgresso();
                } else {
                    JOptionPane.showMessageDialog(null, "Falha no arquivo MIDI.");
                    arquivoMidi = null;
                }
            }
        });
        getContentPane().add(tfNomeArquivo);
        getContentPane().add(btnCarregarArquivo);
    }

    private void configuraSeletorSoundfont() {
        tfNomeSoundfont = new JTextField();
        tfNomeSoundfont.setEditable(false);
        tfNomeSoundfont.setBounds(10, 41, 262, 20);
        tfNomeSoundfont.setText("Arquivo SoundFont");
        btnCarregarSoundfont = new JButton("...");
        btnCarregarSoundfont.setBounds(285, 40, 89, 21);
        btnCarregarSoundfont.setToolTipText("Carregar arquivo SoundFont");
        btnCarregarSoundfont.addActionListener(e ->  {
            String extensoes[] = new String[1];
            extensoes[0] = ".sf2";
            arquivoSoundfont = abrirArquivo("./soundfonts", extensoes, "Arquivos SoundFont (.sf2)");
            if (arquivoSoundfont != null) {
                if (tocador.carregaBancoDeInstrumentos(arquivoSoundfont)) {
                    tfNomeSoundfont.setText(arquivoSoundfont.toString());
                } else {
                    JOptionPane.showMessageDialog(null, "Erro no arquivo soundfont.");
                    arquivoSoundfont = null;
                }
            }
        });

        getContentPane().add(tfNomeSoundfont);
        getContentPane().add(btnCarregarSoundfont);
    }

    private void configuraInformacoes() {
        taInformacoes = new JTextArea();
        taInformacoes.setBounds(33, 135, 301, 191);
        taInformacoes.setEditable(false);

        getContentPane().add(taInformacoes);
    }
    
    private File abrirArquivo(String caminho, String extensoes[], String descricao) {
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
        if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return seletor.getSelectedFile();
        }
        return null;
    }



    public void atualizaInformacoes() {
        StringBuilder sb = new StringBuilder();
        long duracao = (long)tocador.obtemDuracaoNormalSegundos();
        long resolucao = tocador.obtemResolucao();
        double duracao_seminima = tocador.obtemDuracaoSeminima();
        long total_tiques = tocador.obtemTotalTiques();
        double duracao_tique = tocador.obtemDuracaoTique();
        int bpm = tocador.obtemAndamento();
        long total_seminimas = tocador.obtemTotalSeminimas();
        sb.append("Nome do arquivo: ").append(arquivoMidi.getName())
                .append("\nResolução: ").append(resolucao).append(" tiques por semínima")
                .append("\nDuração: ").append(divideTempo(duracao))
                .append("\nTotal de tiques: ").append(total_tiques)
                .append("\nDuração de tique: ").append(duracao_tique).append(" s")
                .append("\nDuração da semínima: ").append(duracao_seminima).append(" s")
                .append("\nNúmero de semínimas: ").append(total_seminimas)
                .append(String.format("\nAndamento: %d bpm", bpm));
        taInformacoes.setText(sb.toString());
    }

    public void atualizaProgresso() {
        long posicaoSegundos = (long)tocador.obtemPosicaoSegundos();
        if (posicaoSegundos != -1) {
            pbProgresso.setValue((int) posicaoSegundos);
            lbProgresso.setText(divideTempo(posicaoSegundos));
        }
    }

    public String divideTempo(long segundos) {
        byte horas = (byte) (segundos / 3600);
        segundos -= horas * 3600;
        byte minutos = (byte) (segundos / 60);
        segundos -= minutos * 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    private void reproduzOuPausa(boolean reproduzindo) {
        btnPausa.setEnabled(reproduzindo);
        btnTocar.setEnabled(!reproduzindo);
        btnParar.setEnabled(true);
    }
    
}
