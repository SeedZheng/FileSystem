package tech.seedhk.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class ByteBuffers  implements Serializable{
	

	private static final long serialVersionUID = 1L;
	private final static int INT_SIZE=4;	//整形长度
	public static byte[] buffer;//数据缓冲区，前10个字节用于存储数据大小 位数不足前置0
	private static char[] chars;//临时存储数据
	private static int index;	//数组指针，存储当前的位置
	
	
	
	public ByteBuffers() {
		buffer=new byte[11];
		chars=new char[16];
	}
	
	public ByteBuffers(int capacity){
		buffer=new byte[capacity];
		chars=new char[capacity];
	}
	
	
	public static void append(char c){
		if(index==chars.length-1)
			reSizeChar(0);
		chars[index]=c;
	}
	
	public static byte[] char2byte(char[] chars){
		int capacity=getcharRealLength(chars);
		if(buffer==null)
			buffer=new byte[capacity+1];
		for(int i=0;i<capacity;i++){
			buffer[10+i]=(byte) chars[i];
		}
		setDataLength(capacity);
		return buffer;
	}
	/**
	 * 读取数据
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] read(InputStream is) throws IOException{
		
		
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<10;i++){
			int a=is.read();
			//if(a!=0)	//这里无需判断是否为0。因为转整数时会自动舍去0
			sb.append((char)a);
		}
		int length=Integer.parseInt(sb.toString().trim());

		byte[] b=new byte[length];
		for(int i=0;i<length;i++){
			int a=is.read();
			b[i]=(byte) a;
			//is.read(b);
		}
		return b;
	}
	
	
	public  void write(OutputStream os,String data) throws IOException{
		
		int length=data.length();
		byte[] head=getDataLength(length);
		byte[] b=data.getBytes();
	
		
		for(int i=0;i<length;i++){
			if(length>=head.length-10)
				reSizeByte(head.length+length+10);
			head[4+i]=b[i];
		}
		os.write(buffer);
		os.flush();
		//return buffer;
	}
	
	public  void write(OutputStream os,Object obj) throws IOException{
	
		byte[] b=ProxyObject.bean2byte(obj);
		
		int length=b.length;
		byte[] head=getDataLength(length);
		
		for(int i=0;i<length;i++){
			if(length>head.length-INT_SIZE) {
				head=grow(head,head.length+length);
			}
			head[INT_SIZE+i]=b[i];
		}
		os.write(head);
		//return buffer;
	}
	
	public  void write(OutputStream os,byte[] data) throws IOException{
		
		int length=getbyteRealLength(data);
		setDataLength(length);
		byte[] b=data;
		
		for(int i=0;i<length;i++){
			if(length>=buffer.length-10)
				reSizeByte(buffer.length+10);
			buffer[10+i]=b[i];
		}
		os.write(buffer);
		//return buffer;
	}
	
	public  void writeObj(OutputStream os,byte[] data) throws IOException{
		
		int length=data.length;
		setDataLength(length);
		byte[] b=data;
		
		for(int i=0;i<length;i++){
			if(length>=buffer.length-10)
				reSizeByte(buffer.length+10);
			buffer[10+i]=b[i];
		}
		os.write(buffer);
		//return buffer;
	}
	
	
	
	public int getcharLength(){
		return chars.length;
	}
	
	public int getRealcharLength(){
		return getcharRealLength(chars);
	}
	
	public static void setDataLength(int data){
		buffer=new byte[11];
		if(buffer!=null){
			byte[] temp=(data+"").getBytes();
			int length=temp.length;  //3
			for(int i=0;i<(10-length);i++){
				buffer[i]=0;
			}
			int j=0;
			for(int i=(10-length);i<10;i++){
				buffer[i]=temp[j];
				j++;
			}
		}
	}
	/**
	 * 获取数据长度
	 * @param data
	 * @return
	 */
	public static byte[] getDataLength(int data) {
		return intToByte4(data);
	}
	
	private static void reSizeChar(int capacity){
		char[] temp=chars;
		if(capacity==0)
			capacity=temp.length<<1;
		chars=Arrays.copyOf(temp, capacity);
	}
	
	private static int getcharRealLength(char[] b){
		int i=0;
		for(;i<b.length;i++){
			if(b[i]=='\0')
				break;
		}
		return i;
	}
	
	
	private static byte[] reSizeByte(int capacity){
		byte[] temp=buffer;
		if(capacity==0)
			capacity=temp.length<<1;
		//buffer=new byte[temp.length<<1];
		return Arrays.copyOf(temp, capacity);
	}
	
	private static byte[] grow(byte[] b,int capacity) {
		if(capacity==0) {
			capacity=b.length<<1;
		}
		return Arrays.copyOf(b, capacity);
	}
	
	
	private static int getbyteRealLength(byte[] b){
		int i=0;
		for(;i<b.length;i++){
			if(b[i]=='\0')
				break;
		}
		return i;
	}
	
	public static void sendMsg(OutputStream os,Integer messageType) throws IOException {
		ByteBuffers buffer=new ByteBuffers();
		buffer.write(os, new Message(messageType));
	}
	
	private static Integer readHead(InputStream is) throws IOException {
		//消息的头十个字节代表数据长度
		byte[] b=new byte[INT_SIZE];
		for(int i=0;i<INT_SIZE;i++){
			int a=is.read();
			//if(a!=0)	//这里无需判断是否为0。因为转整数时会自动舍去0
			b[i]=(byte) a;
		}
		return byte4ToInt(b);
	}
	
	public static Message readMsg(InputStream is) throws IOException {
		
		int length=readHead(is);

		byte[] b=new byte[length];
		
		is.read(b);
		
		Object obj=ProxyObject.byte2Bean(b);
		
		if(obj instanceof Message) {
			return (Message)obj;
		}
		return null;
	}
	
	/**
	 * byte数组转整形
	 * @param bytes
	 * @param off
	 * @return
	 */
	private static int byte4ToInt(byte[] b) {  
		return   b[3] & 0xFF |   
	            (b[2] & 0xFF) << 8 |   
	            (b[1] & 0xFF) << 16 |   
	            (b[0] & 0xFF) << 24;    
    } 
	/**
	 * 整形转byte数组(高位在前，低位在后)
	 * @param i
	 * @return
	 */
	private static byte[] intToByte4(int i) {  
        byte[] targets = new byte[4];  
        targets[3] = (byte) (i & 0xFF);  
        targets[2] = (byte) (i >> 8 & 0xFF);  
        targets[1] = (byte) (i >> 16 & 0xFF);  
        targets[0] = (byte) (i >> 24 & 0xFF);  
        return targets;  
    } 
	
	public static void main(String[] args) {
		int i=123456789;
		byte[] b=intToByte4(i);
		i=byte4ToInt(b);
		System.out.println(i);
	}
		

}
