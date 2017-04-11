import java.awt.event.ActionEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.DecimalFormat;
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
        configuraBoraoEventosMidi();
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
            
            if (tocador.acabou()) {
                btnParar.doClick();
            }
        });

        rastreadorDeProgresso.setRepeats(true);

        setVisible(true);
        atualizaInformacoes();
    }

    private void configuraBoraoEventosMidi(){

        btnEventos = new JButton("BOTAO DE EVENTOS");
        btnEventos.setBounds(10, 72, 364, 23);
        btnEventos.addActionListener(e -> {
            Sequence sequencia = tocador.getSequencia();
            if (sequencia != null) {
                new TelaEventosMidi(sequencia.getTracks());
            }
        });
        getContentPane().add(btnEventos);

    }

    private void configuraBpm(){

        lbBpm = new JLabel("BPM");
        lbBpm.setBounds(33, 366, 66, 41);

        btnAumentarBpm = new JButton("+");
        btnAumentarBpm.setBounds(109, 375, 89, 23);
        btnAumentarBpm.addActionListener((ActionEvent e) -> {
            float velocidade = tocador.getVelocidadeAtual() + 0.1f;
            tocador.controlaAndamento(velocidade);
            atualizaInformacoes();
        });
        
        btnDiminuirBpm = new JButton("-");
        btnDiminuirBpm.setBounds(208, 375, 89, 23);
        btnDiminuirBpm.addActionListener((ActionEvent e) -> {
            float velocidade = tocador.getVelocidadeAtual() - 0.1f;
            tocador.controlaAndamento(velocidade);
            atualizaInformacoes();
        });

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
        pbProgresso.setBounds(10, 502, 364, 14);
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
        lbProgresso = new JLabel("Tempo");
        lbProgresso.setBounds(181, 477, 54, 25);

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
        lbVolume.setBounds(330, 405, 54, 41);

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
                    tfNomeArquivo.setText("Arquivo MIDI:  " + arquivoMidi.toString());
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
        taInformacoes.setBounds(20, 220, 301, 144);
        taInformacoes.setEditable(false);

        getContentPane().add(taInformacoes);
        ImageIcon imagem = new ImageIcon("icons/fundo.png");
        JLabel label = new JLabel();
        label.setBounds(10, 11, 374, 264);
        label.setIcon(imagem);
        getContentPane().add(label);
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
        String velocidade = new DecimalFormat("#.#").format(tocador.getVelocidadeAtual());
        
        sb.append("Nome do arquivo: ");
        
        if (arquivoMidi != null) {
            sb.append(arquivoMidi.getName())
                .append("\nResolução: ").append(resolucao).append(" tiques por semínima")
                .append("\nDuração: ").append(divideTempo(duracao))
                .append("\nTotal de tiques: ").append(total_tiques)
                .append("\nDuração de tique: ").append(duracao_tique).append(" s")
                .append("\nDuração da semínima: ").append(duracao_seminima).append(" s")
                .append("\nNúmero de semínimas: ").append(total_seminimas)
                .append(String.format("\nAndamento: %d bpm", bpm));
        } else {
            sb.append("nenhum arquivo carregado.");
        }
        
        sb.append("\nVelocidade de reproducao: ").append(velocidade).append("x");
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
