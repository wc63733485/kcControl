import com.alibaba.fastjson.JSONObject;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/**
 * @author Michael Huang
 *
 */
public class FrmMain extends Frame {
    Socket s = null;
    DataOutputStream dos = null;
    DataInputStream dis = null;
    private boolean bConnected = false;

    TextField tfTxt = new TextField();
    TextArea taContent = new TextArea();

    Thread tRecv = new Thread(new RecvThread());

    EZioLib.API API = EZioLib.API.INSTANCE;
    public static void main(String[] args) {
        new FrmMain().launchFrame(10080);
    }

    public void launchFrame(int port) {
        setLocation(400, 300);
        this.setSize(300, 300);
        add(tfTxt, BorderLayout.SOUTH);
        add(taContent, BorderLayout.NORTH);
        pack();
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                disconnect();
                System.exit(0);
            }

        });
        tfTxt.addActionListener(new TFListener());
        setVisible(true);
        connect(port);

        tRecv.start();
    }

    public void connect(int port) {
        try {
            s = new Socket("129.211.48.215", port);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());
            bConnected = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disconnect() {
        try {
            dos.close();
            dis.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class TFListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String str = tfTxt.getText().trim();
            tfTxt.setText("");

            try {
                dos.writeUTF(str);
                dos.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }

    private class RecvThread implements Runnable {

        public void run() {
            try {
                while (bConnected) {
                    String str = dis.readUTF();

                    JSONObject jsonObject = JSONObject.parseObject(str);

                    int PosX = 10;
                    int PosY = 10;

                    API.OpenDriver("Godex ZX430i");
                    API.sendcommand("^Q" + Integer.toString(40) + "," + Integer.toString(3));//普通标签纸
                    API.sendcommand("^W" + Integer.toString(40));
                    API.sendcommand("^H" + Integer.toString(10));
                    API.sendcommand("^S" + Integer.toString(3));
                    API.sendcommand("^P" + Integer.toString(1));
                    API.sendcommand("^C" + Integer.toString(jsonObject.getInteger("number")));//打印张数
                    API.sendcommand("^L");
                    API.Bar_QRcode_S(PosX, PosY,jsonObject.getString("data").length(),jsonObject.getString("data"));
                    API.sendcommand("E");
                    API.closeport();

                    taContent.setText(taContent.getText() + "打印张数:" + jsonObject.getInteger("number") + '\n');
                }
            } catch (SocketException e) {
                System.out.println("退出了，bye!");
            } catch (EOFException e) {
                System.out.println("退出了，bye!");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}