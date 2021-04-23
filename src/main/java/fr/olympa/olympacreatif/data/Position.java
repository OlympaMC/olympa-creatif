package fr.olympa.olympacreatif.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class Position {

	private double x = 0;
	private double y = 0;
	private double z = 0;
	private float yaw = 0;
	private float pitch = 0;
	
	public Position() {
	}
	
	public Position(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	public Position(Location loc) {
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
	}
	
	public Location toLoc() {
		return new Location(OlympaCreatifMain.getInstance().getWorldManager().getWorld(), x, y, z, yaw, pitch);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public void teleport(Player p) {
		p.teleport(toLoc());
	}
	
	@Override
	public String toString() {
		return x + ", " + y + ", " + z;
	}
}
