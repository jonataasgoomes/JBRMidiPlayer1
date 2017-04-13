import java.awt.Color;
import java.awt.Container;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.sound.midi.Sequence;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import javax.swing.plaf.metal.MetalSliderUI;

/**
 * Created by Ricardo on 07/04/2017.
 */

public class TelaInicial extends JPanel {

    public JFrame janela;
    
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
    /* Imgem de fundo */
    private BufferedImage imagemFundo;

    public TelaInicial() {

        super();

        tocador = new Tocador();
        janela = new JFrame("Tocador JBR");
        
        setBounds(100, 100, 400, 400);
        janela.setBounds(100, 100, 400, 600);
        janela.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        janela.setResizable(false);
        janela.setLocationRelativeTo(null);
        setLayout(null);
        
        ImageIcon icon = new ImageIcon("./icon.png");
        janela.setIconImage(icon.getImage());
        
        janela.getContentPane().add(this);
        
        try {                
          imagemFundo = ImageIO.read(new File("./icons/fundo.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

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

        janela.setVisible(true);
        atualizaInformacoes();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, getHeight(), new Color(110, 120, 140));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(imagemFundo, 0, 45, this);
    }
    
    // Atalho para pegar content pane da JFrame
    public Container getContentPane() {
        return this;
    }

    private void configuraBoraoEventosMidi(){

        btnEventos = new JButton("Eventos MIDI");
        btnEventos.setBounds(142, 412, 112, 23);
        btnEventos.addActionListener(e -> {
            Sequence sequencia = tocador.getSequencia();
            if (sequencia != null) {
                new TelaEventosMidi(tocador.obtemEventosMidi(), tocador.obtemDuracaoTique());
            }
        });
        btnEventos.setEnabled(false);
        getContentPane().add(btnEventos);

    }

    private void configuraBpm(){

        lbBpm = new JLabel();
        lbBpm.setBounds(12, 366, 192, 41);

        btnAumentarBpm = new JButton("+");
        btnAumentarBpm.setBounds(220, 375, 44, 23);
        btnAumentarBpm.addActionListener((ActionEvent e) -> {
            float velocidade = tocador.getVelocidadeAtual() + 0.1f;
            tocador.controlaAndamento(velocidade);
            atualizaInformacoes();
            mudaLabelVelocidadeReproducao();
        });
        
        btnDiminuirBpm = new JButton("-");
        btnDiminuirBpm.setBounds(272, 375, 44, 23);
        btnDiminuirBpm.addActionListener((ActionEvent e) -> {
            float velocidade = tocador.getVelocidadeAtual() - 0.1f;
            tocador.controlaAndamento(velocidade);
            atualizaInformacoes();
            mudaLabelVelocidadeReproducao();
        });

        getContentPane().add(btnAumentarBpm);
        getContentPane().add(btnDiminuirBpm);
        getContentPane().add(lbBpm);
        mudaLabelVelocidadeReproducao();
    }
    
    private void mudaLabelVelocidadeReproducao() {
        String velocidade = new DecimalFormat("#.#x").format(tocador.getVelocidadeAtual());
        lbBpm.setText("Velocidade de Reprodução: " + velocidade);
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
        slVolume.setBounds(344, 218, 30, 200);
        slVolume.setMaximum(256);
        slVolume.setMinimum(0);
        slVolume.setMajorTickSpacing(10);
        slVolume.setMinorTickSpacing(1);
        slVolume.setValue(256);
        slVolume.setOpaque(false);

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
        tfNomeArquivo = new JTextField() {
            protected void paintComponent(Graphics g)
            {
                g.setColor( getBackground() );
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        tfNomeArquivo.setEditable(false);
        tfNomeArquivo.setBounds(10, 11, 262, 20);
        tfNomeArquivo.setText("Arquivo MIDI");
        tfNomeArquivo.setOpaque(false);
        tfNomeArquivo.setBackground(new Color(255, 255, 255, 205));
        tfNomeArquivo.setForeground(Color.black);
        btnCarregarArquivo = new JButton("Abrir MIDI");
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
                    pbProgresso.setMaximum((int)tocador.obtemDuracaoNormalSegundos());
                    atualizaProgresso();
                    atualizaInformacoes();
                    btnEventos.setEnabled(true);
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
        tfNomeSoundfont = new JTextField() {
            protected void paintComponent(Graphics g)
            {
                g.setColor( getBackground() );
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        tfNomeSoundfont.setEditable(false);
        tfNomeSoundfont.setBounds(10, 41, 262, 20);
        tfNomeSoundfont.setText("Arquivo SoundFont");
        tfNomeSoundfont.setOpaque(false);
        tfNomeSoundfont.setBackground(new Color(255, 255, 255, 205));
        tfNomeSoundfont.setForeground(Color.black);
        btnCarregarSoundfont = new JButton("Abrir SF");
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



    public final void atualizaInformacoes() {
        StringBuilder sb = new StringBuilder();
        long duracao = (long)tocador.obtemDuracaoNormalSegundos();
        long resolucao = tocador.obtemResolucao();
        double duracao_seminima = tocador.obtemDuracaoSeminima();
        long total_tiques = tocador.obtemTotalTiques();
        double duracao_tique = tocador.obtemDuracaoTique();
        int bpm = tocador.obtemAndamento();
        long total_seminimas = tocador.obtemTotalSeminimas();
        
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
        
        taInformacoes.setText(sb.toString());
    }

    public void atualizaProgresso() {
        long posicaoSegundos = (long)tocador.obtemPosicaoSegundos();
        if (posicaoSegundos != -1) {
            pbProgresso.setValue((int) posicaoSegundos);
            lbProgresso.setText(divideTempo(posicaoSegundos));
        }
    }

    public final String divideTempo(long segundos) {
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
