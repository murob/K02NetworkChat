package multichat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;



public class MultiServer implements maxNum {
	
	//멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	//클라이언트 정보저장을 위한 Map 컬렉션 생성
	Map<String, PrintWriter> clientMap;
	
	HashSet<String> blackList;
//	HashSet<String> pWords;
	
	
	//생성자
	public MultiServer() {
		//클라이언트의 이름과 출력스트림을 저장할 HashMap 컬렉션 생성
		clientMap = new HashMap<String, PrintWriter>();
		//HashMap 동기화설정. 쓰레드가 사용자정보에 동시에 접근하는 것을 차단함
		Collections.synchronizedMap(clientMap);
		
		//블랙리스트 셋 선언
		blackList = new HashSet<String>();
		blackList.add("코스모");
		blackList.add("kosmo");
		
		//금칙어 셋 선언
//		pWords = new HashSet<String>();
//		pWords.add("씨발");
//		pWords.add("개새끼");
	}
	
	//채팅 서버 초기화
	public void init() {
		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되엇습니다.");
			
			for(int i=0; i<maxNum.MAX; i++) {
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress() + "(클라이언트)의"
						+socket.getPort()+ "포트를 통해"
						+socket.getLocalAddress()+"(서버)의"
						+socket.getLocalPort()+"포트로 연결되었습니다.");
				
				//쓰레드로 정의된 내부클래스 객체생성 및 시작
				//클라이언트 한명당 하나씩의 쓰레드가 생성된다.
				Thread mst = new MultiServerT(socket);
				mst.start();
				
				if(i==1) {
//					MultiServerT smt = new MultiServerT(socket);
//					smt.out.println("접속자수 제한으로 입장 불가");
					mst.interrupt();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}
	
	//접속된 모든 클라이언트 측으로 서버의 메세지를 Echo해주는 역할 담당
	public void sendAllMsg(String name, String msg, String flag) {
		
		//Map에 저장된 객체의 키값(대화명)을 먼저 얻어온다.
		Iterator<String> it = clientMap.keySet().iterator();
		
		//저장된 객체(클라이언트)의 갯수만큼 반복한다.
		while(it.hasNext()) {
			//각 클라이언트의 PrintWriter객체를 얻어온다.
			try {
				//컬렉션의 key는 클라이언트의 대화명이다.
				String clientName = it.next();
				PrintWriter it_out = (PrintWriter)clientMap.get(clientName);
				
				if(flag.equals("One")) {
					//flag가 One이면 해당 클라이언트 한명에게만 전송한다.(귓속말)
					
					if(name.equals(clientName)) {
						//컬렉션에 저장된 접속자명과 일치하는 경우에만 메세지를 전송한다.
						it_out.println("[귓속말]"+msg);
					}
					
				}
				else {
					//그외에는 모든 클라이언트에게 전송한다.
					/*
					클라이언트에게 메세지를 전달할때 매개변수로 name이
					있는경우와 없는경우를 구분해서 전달하게 된다.
					 */
					if(name.equals("")) {
						//입장, 퇴장에서 사용되는 부분
						it_out.println(URLEncoder.encode(msg, "UTF-8"));
					}
					else {
						//메세지를 보낼때 사용되는 부분
						it_out.println("["+ name +"]:"+ URLEncoder.encode(msg, "UTF-8"));
					}
				}
			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
		}
	}
	
	class MultiServerT extends Thread {
		
		//멤버변수
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		
		public MultiServerT(Socket socket) {//네이버라는 소켓의 쓰레드가 생성..
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
		}
		
		@Override
		public void run() {
			
			String name = "";
			String s = "";
			
			try {
				if(in != null) {
					//클라이언트의 이름을 읽어온다.
					name = in.readLine();
					name = URLDecoder.decode(name, "UTF-8");
					
					Iterator<String> itr = clientMap.keySet().iterator();
					while(itr.hasNext()) {
						String clientName = itr.next();
						if(name.equals(clientName)) {
							System.out.println("중복된 이름이 존재합니다.");
							this.interrupt();
							name=name+"temp";
							in.close();
							out.close();
							socket.close();
							return;
						}
					} 
					
					//블랙리스트 설정
					Iterator it = blackList.iterator();
					while(it.hasNext()) {
						String list = (String)it.next();
						if(name.equals(list)) {
							System.out.println("해당이름으론 만들 수 없습니다.");
							this.interrupt();
							name=name+"temp";
							in.close();
							out.close();
							socket.close();
							return;
						}
					}
					
					//대화 금칙어 처리
//					Iterator ite = pWords.iterator();
//					while(ite.hasNext()) {
//						String list = (String)ite.next();
//						if(s.equals(list)) {
//							s="금지된언어입니다.";
////							s=s+"temp";
////							in.close();
////							out.close();
////							socket.close();
////							return;
//						}
//					}
					
					//방금 접속한 클라이언트를 제외한 나머지에게 입장을 알린다.
					sendAllMsg("", name+"님이 입장하셨습니다.", "All");
					//현재 접속한 클라이언트를 HashMap에 저장한다.
					clientMap.put(name, out);
					
					//접속자의 이름을 서버의 콘솔에 띄워주고
					System.out.println(name +" 접속");
					//HashMap에 저장된 객체의 수로 현재 접속자를 파악할 수 있다.
					System.out.println("현재 접속자 수는"+ clientMap.size()+"명 입니다.");
					
					//입력한 메세지는 모든 클라이언트에게 Echo된다.
					while(in != null) {
						s = in.readLine();
						s = URLDecoder.decode(s, "UTF-8");
//						System.out.println("s");
						
						if(s == null) break;
						//서버의 콘솔에 출력되고..
						System.out.println(name +" >> " + s);
						
						//클라이언트 측으로 전송한다.
						if(s.charAt(0)=='/') {
							String[] strArr = s.split(" ");
							String msgContent = "";
							for(int i=2; i<strArr.length; i++) {
								msgContent += strArr[i]+" ";
							}
							if(strArr[0].equals("/to")) {
								sendAllMsg(strArr[1], msgContent, "One");
							}
						}
						else {
							sendAllMsg(name, s, "All");
						}
					}
				}
			}
			/*
			클라이언트가 접속을 종료하면 Socket예외가 발생하게 되어
			finally절로 진입하게 된다. 이때 "대화명"을 통해 정보를
			삭제한다.
			 */
			catch (Exception e) {
				System.out.println("예외1:"+ e);
			}
			finally {
				
				try {
					clientMap.remove(name);
					sendAllMsg("", name + "님이 퇴장하셨습니다.", "All");
					System.out.println(name + " ["+
							Thread.currentThread().getName()+"] 퇴장");
					System.out.println("현재 접속자 수는"+clientMap.size()+"명 입니다.");
					in.close();
					out.close();
					socket.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}	
	}
}
