package tech.seedhk.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {
	
	
	public static String[] listRoots(){
		File[] roots=File.listRoots();
		String[] strs=new String[roots.length];
		for(int i=0;i<roots.length;i++){
			File file=roots[i];
			strs[i]=file.getAbsolutePath();
		}
		return strs;
	}
	
	public static String[] showDire(String path){
		
		String[] fileNames=null;
		File[] files=null;
		if("root".equals(path)){
			 files=File.listRoots();
			 fileNames=new String[files.length];
			 for(int i=0;i<files.length;i++){
					File file=files[i];
					fileNames[i]=file.getAbsolutePath();
				}
		}else{
			File file=new File(path);
			if(file.exists() && file.isDirectory()){
				 files=file.listFiles();
			}
			if(!isEmpty(files)){
				fileNames=new String[files.length];
				for(int i=0;i<files.length;i++){
					fileNames[i]=files[i].getName()+"\n";
				}
			}
		}
		
		
		return fileNames;
	}
	
	public static List<Map<String, String>> listDirectory(String path){
		
		List<Map<String, String>> list=new ArrayList<>();
		
		if(isEmpty(path))
			return new ArrayList<>(0);
		File file=new File(path);
		if(!file.exists())
			return new ArrayList<>(0);
		
		File[] files=file.listFiles();
		//String[] strs=new String[files.length];
		if(!isEmpty(files)){
			for(int i=0;i<files.length;i++ ){
				Map<String,String> map=new HashMap<>();
				if(files[i].isDirectory())
					map.put("type", "1");
				else
					map.put("type", "0");
				map.put("path", files[i].getAbsolutePath());
				//strs[i]=files[i].getAbsolutePath();
				list.add(map);
			}
		}
		return list;
	}
	/**
	 * 获取某个文件夹的父目录
	 * @param path
	 * @return
	 */
	public static String getParent(String path){
		if(isEmpty(path))
			return "";
		File file=new File(path);
		if(!file.exists())
			return "";
		String parent=file.getParent();
		if(isEmpty(parent))
			return "";
		return parent;
		
	}
	
	public static boolean isEmpty(Object str){
		if(null==str || "".equals(str.toString()))
			return true;
		return false;
	}
	
	public static void main(String[] args) {
		//测试汉子“向”的acsii编码
		String str="向";
		byte[] b=new byte[str.length()];
		b=str.getBytes();
		System.out.println(b);
	}

}
