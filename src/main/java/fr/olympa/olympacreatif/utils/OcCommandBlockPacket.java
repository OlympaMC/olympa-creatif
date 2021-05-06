package fr.olympa.olympacreatif.utils;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PacketPlayOutTileEntityData;

public class OcCommandBlockPacket extends PacketPlayOutTileEntityData {

	public OcCommandBlockPacket (BlockPosition pos, NBTTagCompound tag) {
		super(pos, 2, tag);
	}
}
