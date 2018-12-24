package tech.seedhk.buffer;

import java.io.Serializable;

/**
 * 包含数据：
 *  1.body大小
 *  2.body块数量
 *  4.head开始标志  hs_$
 *  5.head结束标志  he_$
 *  将head的大小控制在128KB内
 * @author Seed
 * 2017年12月27日 下午1:45:59
 */
public class HeadBuffer implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private final static byte[] head_start;
	private final static byte[] head_end;
	private long body_size;
	
	static{
		head_start="hs_$".getBytes();
		head_end="he_$".getBytes();
	}

	
	public long getBody_size() {
		return body_size;
	}

	public void setBody_size(long body_size) {
		this.body_size = body_size;
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static byte[] getHeadStart() {
		return head_start;
	}

	public static byte[] getHeadEnd() {
		return head_end;
	}
	

}
