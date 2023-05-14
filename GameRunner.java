import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class GameRunner {
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static JTextField textField;
    
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        GameController gameController = new GameController();
        frame.add(gameController);
        frame.setUndecorated(true);
        frame.setSize(screenSize);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}