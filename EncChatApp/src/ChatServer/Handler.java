package ChatServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import protocol.KeyTool;
import protocol.SimpleProtocol;

public class Handler implements Runnable {

	private Socket socket = null;
	private SimpleProtocol protocol = new SimpleProtocol();
	private BufferedReader in;
	private DataOutputStream out;
	private Server server;
	private String username;
	private Key key2;
	private Cipher AESCipher;

	public Handler(Socket socket) {
		this.socket = socket;
	}

	public void sendToClient(String... args) {
		try {
			// create protocol message
			String protocolMessage = protocol.createMessage(args);
			// aes encryption mode

			AESCipher.init(Cipher.ENCRYPT_MODE, key2);

			// encrypt to bytes
			byte[] bytes = AESCipher.doFinal(protocolMessage.getBytes());
			// base64 encoder
			String string = Base64.getEncoder().encodeToString(bytes);
			// write bytes
			out.writeBytes((string + "\n"));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}

	public String[] getFromClient() throws Exception {
		// read message in base 64 it
		byte[] messageIn = Base64.getDecoder().decode(in.readLine());
		// aes decrypt mode
		AESCipher.init(Cipher.DECRYPT_MODE, key2);
		byte[] bytes = AESCipher.doFinal(messageIn);
		return protocol.decodeMessage(new String(bytes));
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			server = Server.getInstance();

			// read line
			String firstMes = in.readLine();
			// create RSA cipher
			Cipher RSA_cipher = Cipher.getInstance("RSA");

			// intialise rsa decrypt with private key
			RSA_cipher.init(Cipher.DECRYPT_MODE, KeyTool.getRSAPrivateKey());

			// decode
			byte[] bytes = Base64.getDecoder().decode(firstMes);
			byte[] Key1Bytes = RSA_cipher.doFinal(bytes); // depends on the mode
			// get key1
			Key key1 = new SecretKeySpec(Key1Bytes, "AES");

			// create aes cipher
			AESCipher = Cipher.getInstance("AES");
			// ecrypt mode with key1
			AESCipher.init(Cipher.ENCRYPT_MODE, key1);

			// create new aes key
			key2 = KeyTool.getAESKey();

			// bytes out fro key2
			byte[] bytesOut = AESCipher.doFinal((key2.getEncoded()));
			//base 64 encoder
			String stringOut = Base64.getEncoder().encodeToString(bytesOut);
			//write key2 to client
			out.writeBytes(stringOut + "\n");

			// Sign in or create account
			String[] message = getFromClient();
			switch (message[0]) {
			case "sign-in": {
				if (server.users.containsKey(message[1])) {
					if (server.users.get(message[1]).equals(message[2])) {
						this.username = message[1];
						sendToClient("sign-in", "true", "welcome");
					} else {
						sendToClient("sign-in", "false", "Username and password do not match");
						return;
					}
				} else {
					sendToClient("sign-in", "false", "Username does not exist");
					return;
				}
				break;
			}
			case "sign-up": {
				if (false == server.users.containsKey(message[1])) {
					server.users.put(message[1], message[2]);
					sendToClient("sign-up", "true", "Registration successfully!");
				} else {
					sendToClient("sign-up", "false", "Username exists.");
				}
				return;
			}
			default:
				return;
			}
			SimpleDateFormat dFormat = new SimpleDateFormat("hh:mm");
			while (true) {
				message = getFromClient();
				switch (message[0]) {
				case "send-message": {
					server.messages.add(new Message(username, new Date(), message[1]));
					sendToClient("send-message", "true", "ok!");
					break;
				}
				case "get-message": {
					int offset = Integer.parseInt(message[1]);
					if (offset < -1)
						offset = -1;
					ArrayList<String> newMessages = new ArrayList<>();
					newMessages.add("get-message");
					for (int i = offset + 1; i < server.messages.size(); i++) {
						newMessages.add(Integer.toString(i));
						newMessages.add(server.messages.get(i).getUsername());
						newMessages.add(dFormat.format(server.messages.get(i).getTimestamp()));
						newMessages.add(server.messages.get(i).getContent());
					}
					if (newMessages.size() < 1) {
						out.writeBytes("\n");
					}
					sendToClient(newMessages.toArray(new String[newMessages.size()]));
					break;
				}
				default:
					return;
				}
			}

		} catch (Exception e) {
			try {
				socket.close();
				e.printStackTrace();
				return;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
