import javax.annotation.processing.FilerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    public final static int SERVICE_PORT = 69;

    public static void main(String[] args) throws IOException {
        try {
            DatagramSocket serverSocket = new DatagramSocket(SERVICE_PORT);
            String filename = null;
            String mode;
            byte[] fileByte;
            int block;
            while (true) {
                byte[] receivingDataBuffer = new byte[516];
                DatagramPacket outputPacket;
                ArrayList<byte[]> fileBlocks = new ArrayList<>();
                /* Создайте экземпляр UDP-пакета для хранения клиентских данных с использованием буфера для полученных данных */
                DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
                System.out.println("Waiting for a client to connect...");

                // Получите данные от клиента и сохраните их в inputPacket
                serverSocket.receive(inputPacket);
                byte[] packet = inputPacket.getData();


                InetAddress senderAddress = inputPacket.getAddress();
                int senderPort = inputPacket.getPort();

                System.out.println(Arrays.toString(packet));
                if (packet[1] == 2) {
                    Put message = new Put(packet);
                    filename = message.getName();
                    mode = message.getMode();
                    byte[] ack = new byte[]{0, 4, 0, 0};
                    outputPacket = new DatagramPacket(ack, ack.length, senderAddress, senderPort);
                    serverSocket.send(outputPacket);


                    boolean flag = true;
                    while (flag) {
                        receivingDataBuffer = new byte[517];
                        inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
                        serverSocket.receive(inputPacket);
                        int size =  inputPacket.getLength();
                        packet = inputPacket.getData();
                        if (packet[1] == 3) {
                            message = new Put(packet);
                            block = message.getBlock();
                            fileByte = message.getFileByte(size - 4);
                            fileBlocks.add(block - 1 , fileByte);
                            if (size - 4 < 512){
                                flag = false;
                            }
                            ack = new byte[]{0, 4, packet[2], packet[3]};
                            outputPacket = new DatagramPacket(ack, ack.length, senderAddress, senderPort);
                            serverSocket.send(outputPacket);
                        }
                    }

                    byte[] firstBlock = fileBlocks.get(0);
                    byte[] result = Arrays.copyOf(firstBlock , (fileBlocks.size() - 1 )* 512 + fileBlocks.get(fileBlocks.size() - 1).length);
                    for (int j = 1 ; j < fileBlocks.size() - 1 ; j++){
                        System.arraycopy(fileBlocks.get(j) , 0, result, j * 512 , 512);
                    }
                    System.arraycopy(fileBlocks.get(fileBlocks.size() - 1) , 0, result, (fileBlocks.size() - 1 )* 512 , fileBlocks.get(fileBlocks.size() - 1).length);
                    System.out.println(result.length);
                    FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "\\getfile\\" + filename);
                    fos.write(result, 0, result.length);
                    fos.close();
                } else if(packet[1] == 1) {
                    Get message = new Get(packet);
                    filename = message.getName();
                    mode = message.getMode();
                    String path = System.getProperty("user.dir") + '\\' + "getfile\\" + filename;
                    System.out.println(path);
                    byte[] fileInArray = new byte[0];
                    try {
                        File file = new File(path);
                        fileInArray = new byte[(int)file.length()];
                        FileInputStream fileStream = new FileInputStream(file);
                        fileStream.read(fileInArray);
                        System.out.println(Arrays.toString(fileInArray));
                    } catch (IOException e){
                        byte[] error = new byte[]{0, 5, 0, 1 , 0};
                        outputPacket = new DatagramPacket(error, error.length, senderAddress, senderPort);
                        serverSocket.send(outputPacket);
                        continue;
                    }



                    ArrayList<byte[]> fileBytes = new ArrayList<>();
                    block = 1;
                    for (int i = 0 ; i < fileInArray.length ; i += 512){
                        System.out.println(fileInArray.length);
                        System.out.println(block);
                        byte[] OneBytes = new byte[512];
                        if(fileInArray.length > 512*(block)) {
                            OneBytes = new byte[512];
                            System.arraycopy(fileInArray, i, OneBytes, 0, 512);
                        } else {
                            OneBytes = new byte[fileInArray.length - (512*(block - 1))];
                            System.arraycopy(fileInArray, i, OneBytes, 0, fileInArray.length - (512*(block - 1)));
                        }
                        fileBytes.add(OneBytes);
                        block++;
                    }

                    System.out.println(fileBytes.get(fileBytes.size() - 1).length);

                    block=1;
                    for (byte[] by : fileBytes) {
                        byte[] data = new byte[4 + by.length]; //

                        data[0] = 0;
                        data[1] = 3;


                        byte[] byFile = new byte[]{
                                (byte) (block  >> 8),
                                (byte) block
                        };

                        data[2] = byFile[0];
                        data[3] = byFile[1];

                        System.arraycopy(by, 0, data, 4, by.length);
                        System.out.println(Arrays.toString(data));
                        outputPacket = new DatagramPacket(data, data.length, senderAddress, senderPort);
                        serverSocket.send(outputPacket);
                        receivingDataBuffer = new byte[517];
                        inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
                        serverSocket.receive(inputPacket);
                        packet = inputPacket.getData();
                        System.out.println(Arrays.toString(packet));
                        
                        if (packet[1] != 4 || packet[2] != byFile[0] || packet[3] != byFile[1]) {
                            System.out.println("Connect lost");
                            System.exit(1);
                        }
                        block++;
                    }
                }


            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}