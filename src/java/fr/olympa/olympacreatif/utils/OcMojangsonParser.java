package fr.olympa.olympacreatif.utils;

import org.bukkit.entity.Player;

import com.mojang.brigadier.StringReader;

import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

public class OcMojangsonParser extends MojangsonParser {

	public OcMojangsonParser(String tag) {
		super(new OcStringReader(tag));
	}

	public NBTTagCompound parse() {
		return parseFor(null);
	}

	public NBTTagCompound parse(Player p) {
		return parseFor(p);
	}
	
	private NBTTagCompound parseFor(Player p) {
		try {
			return f();
		} catch (Exception e) {
			if (p != null)
				p.sendMessage("§cUne erreur est survenue lors du séquençage du tag, veuillez vérifier votre commande : §7" + e.getMessage());
			return new NBTTagCompound();
		}
	}
	
}



class OcStringReader extends StringReader {

	public OcStringReader(String string) {
		super(string);
	}
	
	@Override
	public int readInt() throws IllegalArgumentException {
		int start = getCursor();
		
		while (canRead() && isAllowedNumber(peek())) {
			skip();
		}
		
		String number = getString().substring(start, getCursor());
		
		if (number.isEmpty()) {
			throw new IllegalArgumentException("Catched error while parsing an INTEGER tag!");
		}
		
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException ex) {
			setCursor(start);
			throw new IllegalArgumentException("Catched error while parsing an INTEGER tag!");
		} 
	}

	
	@Override
	public long readLong() throws IllegalArgumentException {
		int start = getCursor();
		
		while (canRead() && isAllowedNumber(peek())) {
			skip();
		}
		
		String number = getString().substring(start, getCursor());
		
		if (number.isEmpty()) {
			throw new IllegalArgumentException("Catched error while parsing a LONG tag!");
		}
		
		try {
			return Long.parseLong(number);
		} catch (NumberFormatException ex) {
			setCursor(start);
			throw new IllegalArgumentException("Catched error while parsing a LONG tag!");
		} 
	}
	
	@Override
	public double readDouble() throws IllegalArgumentException {
		int start = getCursor();
		
		while (canRead() && isAllowedNumber(peek())) {
			skip();
		}
		
		String number = getString().substring(start, getCursor());
		
		if (number.isEmpty()) {
			throw new IllegalArgumentException("Catched error while parsing a DOUBLE tag!");
		}
		
		try {
			return Double.parseDouble(number);
		} catch (NumberFormatException ex) {
			setCursor(start);
			throw new IllegalArgumentException("Catched error while parsing a DOUBLE tag!");
		} 
	}
	
	@Override
	public float readFloat() throws IllegalArgumentException {
		int start = getCursor();
		
		while (canRead() && isAllowedNumber(peek())) {
			skip();
		}
		
		String number = getString().substring(start, getCursor());
		
		if (number.isEmpty()) {
			throw new IllegalArgumentException("Catched error while parsing a FLOAT tag!");
		}
		
		try {
			return Float.parseFloat(number);
		} catch (NumberFormatException ex) {
			setCursor(start);
			throw new IllegalArgumentException("Catched error while parsing a FLOAT tag!");
		} 
	}
	
	@Override
	public String readUnquotedString() {
		int start = getCursor();
		
		while (canRead() && isAllowedInUnquotedString(peek())) {
			skip();
		}
		
		return getString().substring(start, getCursor());	
	}

	@Override
	public String readQuotedString() throws IllegalArgumentException {
		if (!canRead()) {
			return "";
		}
		
		char next = peek();
		if (!isQuotedStringStart(next)) {
			throw new IllegalArgumentException("Catched error while parsing an UNQUOTED STRING tag!");
		}
		
		skip();
		return readStringUntil(next);
	}
	
	@Override
	public String readStringUntil(char terminator) throws IllegalArgumentException {
		StringBuilder result = new StringBuilder();
		boolean escaped = false;
		
		while (canRead()) {
			char c = read();
			if (escaped) {
				if (c == terminator || c == '\\') {
					result.append(c);
					escaped = false; continue;
				}
				
				setCursor(getCursor() - 1);
				throw new IllegalArgumentException("Catched error while parsing a tag!");
			} 
			
			if (c == '\\') {
				escaped = true; 
				continue;
			}if (c == terminator) {
				return result.toString();
			}
			
			result.append(c);
		} 

		
		throw new IllegalArgumentException("Catched error while parsing a tag!");
		}
	
	
	
	@Override
	public String readString() {
		if (!canRead()) {
			return "";
		}
		char next = peek();
		if (isQuotedStringStart(next)) {
			skip();
			return readStringUntil(next);
		} 
		return readUnquotedString();
	}
	  
	@Override
	public boolean readBoolean() throws IllegalArgumentException {
		int start = getCursor();
		String value = readString();
		
		if (value.isEmpty()) {
			new IllegalArgumentException("Catched error while parsing a BOOLEAN tag!");
		}
		
		if (value.equals("true"))
			return true; 
		if (value.equals("false")) {
			return false;
		}
		
		setCursor(start);
		throw new IllegalArgumentException("Catched error while parsing a BOOLEAN tag!");
	}
	  
	  
	@Override
	public void expect(char c) throws IllegalArgumentException {
		if (!canRead() || peek() != c) 
			throw new IllegalArgumentException("Catched error while parsing a tag, invalid symbol detected!");
		
		skip();
  }
}