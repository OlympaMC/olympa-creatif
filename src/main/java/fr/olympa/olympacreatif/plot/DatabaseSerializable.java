package fr.olympa.olympacreatif.plot;

public interface DatabaseSerializable {

	public String toDbFormat();
	
	public static Object fromDbFormat(String data) {
		return null;
	}
}
