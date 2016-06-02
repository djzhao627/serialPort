package com.lewei.Simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SerialPortScreen extends JFrame implements Runnable, SerialPortEventListener {

	private static final long serialVersionUID = -3229445339967173429L;
	private JPanel contentPane;
	private JTable table;
	private List list;

	/** �˿ڻ�ȡ�� */
	static CommPortIdentifier portId;
	/** �˿ڼ��� */
	static Enumeration<?> portList;
	/** ���ں� */
	private static String comPort = "";

	InputStream inputStream;
	SerialPort serialPort;
	Thread readThread;
	private int numBytes;
	/** ʱ���ʽ�� */
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/** ��ͷ */
	String[] heads = { "��ť", "״̬", "ʱ��" };
	/** ��ģ�� */
	private DefaultTableModel model = new DefaultTableModel(new String[][] {}, heads);
	/** ��Ԫ����Ⱦ�� */
	DefaultTableCellRenderer render = new DefaultTableCellRenderer();

	/** ��ʼ������״̬ */
	private String[] boardsState = { "00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000",
			"00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000",
			"00000000", "00000000", "00000000", "00000000" };

	/** ����ID */
	private String boardID = "";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					File file = new File("C:\\config\\port.txt");
					if (file.isFile() && file.exists()) {
						FileInputStream fis = new FileInputStream(file);
						InputStreamReader isr = new InputStreamReader(fis);
						BufferedReader br = new BufferedReader(isr);
						String line = null;
						boolean isFound = false;
						while ((line = br.readLine()) != null) {
							if (line.startsWith("COM:")) {
								isFound = true;
								comPort = line.substring(4);
								break;
							}
						}
						br.close();
						if(!isFound){
							JOptionPane.showMessageDialog(null, "������Ϣ����ȷ�����飡");
							System.exit(0);
						}
						org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
						UIManager.put("RootPane.setupButtonVisible", false);
						SerialPortScreen frame = new SerialPortScreen();
						frame.setVisible(true);
					} else {
						JOptionPane.showMessageDialog(null, "�޷����������ļ������飡");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SerialPortScreen() {
		setMinimumSize(new Dimension(300, 200));
		setTitle("\u6309\u952E\u76D1\u63A7");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getResource("/images/Wittur_Logo.gif")).getImage());
		setBounds(100, 100, 604, 429);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		list = new List();
		contentPane.add(list, BorderLayout.WEST);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0)); 

		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);

		table = new JTable();
		table.setSelectionBackground(new Color(204, 255, 255));
		table.setShowVerticalLines(false);
		table.setFont(new Font("����", Font.ITALIC, 12));
		table.setBorder(null);
		// ������������
		table.getTableHeader().setReorderingAllowed(false);
		table.setModel(model);
		render.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(render);
		table.getColumnModel().getColumn(1).setCellRenderer(render);
		table.getColumnModel().getColumn(2).setCellRenderer(render);
		scrollPane.setViewportView(table);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		contentPane.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));

		JLabel lblNewLabel = new JLabel("\u4E0A\u7EBF\u6A21\u5757");
		panel_1.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("\u6309\u94AE\u72B6\u6001");
		panel_1.add(lblNewLabel_1);
		receiveData().execute();
	}

	/**
	 * ��̨��أ����ݽ��ա�
	 * 
	 * @return
	 */
	SwingWorker<Void, Void> receiveData() {
		SwingWorker<Void, Void> receive = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() {

				// ��ȡ���ڵĶ˿�
				portList = CommPortIdentifier.getPortIdentifiers();
				while (portList.hasMoreElements()) {
					portId = (CommPortIdentifier) portList.nextElement();
					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						if (portId.getName().equals(comPort)) {
							SimpleRead();
						}
					}
				}

				return null;
			}
		};
		return receive;
	}

	public void SimpleRead() {
		try {
			serialPort = (SerialPort) portId.open("Reader", 1000);
		} catch (PortInUseException e) {
			JOptionPane.showMessageDialog(null, comPort + "�˿ڱ�ռ�ã����飡");
			System.exit(0);
		}
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
		}
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
		}
		readThread = new Thread(this);
		readThread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
	}

	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[16];
			try {
				while (inputStream.available() > 0) {
					numBytes = inputStream.read(readBuffer);
					// System.out.println(numBytes);
				}
				String reader = (new String(readBuffer, 0, numBytes)).trim();
				// for instance : 2E 7B 05 05 01 00 00 00
				if (reader.startsWith("2E7B05") && reader.length() == 16) {
					// ��ȡ���
					int board = Integer.valueOf(reader.substring(6, 8), 16);
					// תΪ������
					String key = "00000000" + Integer.toBinaryString(Integer.valueOf(reader.substring(8, 10), 16));
					key = key.substring(key.length() - 8, key.length());
					char[] keys = key.toCharArray();
					char[] boardKeys = boardsState[board - 1].toCharArray();
					boolean isSame = true;
					for (int i = 0; i < 8; i++) {
						if (keys[i] != boardKeys[i]) {
							isSame = false;
							String state = "����";
							if (keys[i] == '1') {
								state = "����";
							} else {
								state = "̧��";
							}
							// ��ť����
							model.addRow(new String[] { board + "�Ű壬��ť��" + (8 - i), state, sdf.format(new Date()) });
						}
					}
					// ����Դ״̬
					if (!isSame) {
						boardsState[board - 1] = key;
					}

					// ģ�����߼��
					if (!boardID.contains("<" + board + ">")) {
						boardID += "<" + board + ">";
						list.add("  " + board);
					}
				}
			} catch (IOException e) {
			}
			break;
		}
	}

}
