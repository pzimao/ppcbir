package cn.edu.uestc.cbir;

import cn.edu.uestc.utils.LogUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class Client {
    private Socket socket;

    public Client(String host, int port) {
        try {
            socket = new Socket();
            // 关闭socket时，立即释放socket绑定端口以便端口重用，默认为false
            socket.setReuseAddress(true);
            // 关闭传输缓存，默认为false
            socket.setTcpNoDelay(true);
            // 如果输入流等待1000毫秒还未获得服务端发送数据，则提示超时，0为永不超时
            socket.setSoTimeout(0);
            // 关闭socket时，底层socket不会直接关闭，会延迟一会，直到发送完所有数据
            // 等待10秒再关闭底层socket连接，0为立即关闭底层socket连接
            socket.setSoLinger(true, 0);
            // 设置性能参数，可设置任意整数，数值越大，相应的参数重要性越高（连接时间，延迟，带宽）
            socket.setPerformancePreferences(3, 4, 5);
            SocketAddress address = new InetSocketAddress(host, port);
            // socket创建超时时间为3000毫秒
            socket.connect(address, 3000);

        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.log("Cilent socket establish failed!");
        }
    }

    @SuppressWarnings("unchecked")
    public Object inquery(Message message) {
//		ArrayList<Float[][]> result2sList = null;
        Object result2sList = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            Object obj = objectInputStream.readObject();
            if (obj != null) {
                // 处理接收到的对象
//				result2sList = (ArrayList<Float[][]>) obj;
                result2sList = obj;

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result2sList;
    }

    public HashMap<String, HashMap<Integer, Short[]>>[] inquery5(Message message) {
        HashMap<String, HashMap<Integer, Short[]>>[] resultDistanceMapArray = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();

            ObjectOutputStream oos = null;
            GZIPInputStream gzipis = null;
            ObjectInputStream ois = null;
            gzipis = new GZIPInputStream(socket.getInputStream());
            ois = new ObjectInputStream(gzipis);
            Object obj = ois.readObject();
//			objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
//			Object obj = objectInputStream.readObject();
            if (obj != null) {
                // 处理接收到的对象
                resultDistanceMapArray = (HashMap<String, HashMap<Integer, Short[]>>[]) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultDistanceMapArray;
    }
}