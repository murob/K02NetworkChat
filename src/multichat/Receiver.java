package multichat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;

//서버가 보내는 Echo메세지를 읽어오는 쓰레드 클래스
public class Receiver extends Thread {
	
	Socket socket;
	BufferedReader in = null;
	
	//클라이언트가 접속할때 사용한 Socket객체를 기반으로 입력스트림 생성
	public Receiver(Socket socket) {
		
		this.socket = socket;
		
		try {
//			in.readLine();
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
		}
//		catch(NullPointerException e) {
//			System.out.println("중복값입니다.");
//		}
		catch (Exception e) {
			System.out.println("예외>Receiver>생성자:"+ e);
		}
	}
	
	/*
	run()메소드에서는 서버가 보내는 Echo메세지를
	지속적으로 읽어오고, 예외발생시 while루프를 탈출한다.
	 */
	@Override
	public void run() {
		while(in != null) {
			try {
				//서버에서 내려온 한글 데이터 사용을 위해 UTF-8로 디코딩
				String a = in.readLine();
				if(a==null) {
					break;
				}	
				else {
					System.out.println(">>" + URLDecoder.decode(a, "UTF-8"));
				}
			}
			catch (SocketException e) {
				System.out.println("SocketException");
				break;
			}
			catch (Exception e) {
				System.out.println("예외>Receiver>run1:"+ e);
			}
		}
		
		try {
			in.close();
		} catch (Exception e) {
			System.out.println("예외>Receiver>run2:"+e);
		}
	}
}
