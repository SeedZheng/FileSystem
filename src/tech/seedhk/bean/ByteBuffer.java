package tech.seedhk.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class ByteBuffer  implements Serializable{
	

	private static final long serialVersionUID = 1L;
	public static byte[] buffer;//数据缓冲区，前10个字节用于存储数据大小 位数不足前置0
	private static char[] chars;//临时存储数据
	private static int index;	//数组指针，存储当前的位置
	
	
	
	public ByteBuffer() {
		buffer=new byte[11];
		chars=new char[16];
	}
	
	public ByteBuffer(int capacity){
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
		setDataLength(length);
		byte[] b=data.getBytes();
	
		
		for(int i=0;i<length;i++){
			if(length>=buffer.length-10)
				reSizeByte(buffer.length+10);
			buffer[10+i]=b[i];
		}
		os.write(buffer);
		os.flush();
		//return buffer;
	}
	
	public  void write(OutputStream os,Object obj) throws IOException{
	
		byte[] b=ProxyObject.bean2byte(obj);
		
		int length=b.length;
		setDataLength(length);
		
		for(int i=0;i<length;i++){
			if(length>=buffer.length-10)
				reSizeByte(buffer.length+10);
			buffer[10+i]=b[i];
		}
		os.write(buffer);
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
	
	
	private static void reSizeByte(int capacity){
		byte[] temp=buffer;
		if(capacity==0)
			capacity=temp.length<<1;
		//buffer=new byte[temp.length<<1];
		buffer=Arrays.copyOf(temp, capacity);
	}
	private static int getbyteRealLength(byte[] b){
		int i=0;
		for(;i<b.length;i++){
			if(b[i]=='\0')
				break;
		}
		return i;
	}
	
	

}
