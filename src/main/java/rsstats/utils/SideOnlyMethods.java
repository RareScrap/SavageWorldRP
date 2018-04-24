package rsstats.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class SideOnlyMethods {

    @SideOnly(Side.CLIENT)
    public static EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }
}
