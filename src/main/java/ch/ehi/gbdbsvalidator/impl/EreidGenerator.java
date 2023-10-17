package ch.ehi.gbdbsvalidator.impl;


public class EreidGenerator {
	private EreidGenerator(){}

	public static boolean validateEreid(String ereid)
	{
		if(ereid.length()<6 || ereid.length()>22){ 
			return false;
		}
		String value=ereid.substring(0,ereid.length()-2);
		String pzStr=ereid.substring(ereid.length()-2);
		//System.out.println("value <"+value+">");
		//System.out.println("pz <"+pzStr+">");
		int pz=0;
		try {
			pz = Integer.parseInt(pzStr);
		} catch (java.lang.NumberFormatException e) {
			return false;
		}
		int calcPz = (int)(calcChecksum(value));
		//System.out.println("calcpz <"+calcPz+">");
		if(pz!=calcPz){
			return false;
		}

		return true;
		
	}
	public static String checksum(String id)
	{
		if(id.length()>20){
			throw new IllegalArgumentException("EREID to long "+id);
		}
		String check="";
		long sum = calcChecksum(id);
		if( sum == 0){
			check = "00";
		}else{
			check = right("0" + Long.toString(sum), 2);
		}
		return id+check;
	}

	private static long calcChecksum(String id) {
		long sum=0;
		for(char c : id.toCharArray()){
			if (c >= '0' && c <= '9') {
		        sum += c - '0';
		    }else if (c >= 'A' && c <= 'Z') {
		        sum += c - 'A' + 10;
		    }else if (c >= 'a' && c <= 'z') {
		        sum += c - 'a' + 36;
		    }else if (c=='.') {
		        sum += 62;
		    }else if (c=='-') {
		        sum += 63;
		    }else if (c=='/') {
		        sum += 64;
		    }else{
		        sum += 65;
		    }
		}
		sum=sum%97;
		return sum;
	}
	private static String right(String value,int length)
	{
		int len=value.length();
		if(len<length){
			return value;
		}
		return value.substring(len-length);
	}
	public static void main(String args[])
	{
    	//String ereid="SX1367940612002aiHQt86";
    	//String ereid=checksum("CH5483100019901.011E1");
    	//String ereid=checksum("CH"+"4955"+"999.001E1");
    	String ereid="CH303500/6-4-013427/30";
    	//String ereid="CH303500/6-4-013428/31";
    	
		System.out.println(ereid);
		System.out.println("validateEreid "+validateEreid(ereid));
	}
}
