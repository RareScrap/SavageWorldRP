package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import rsstats.data.ExtendedPlayer;

import java.util.ArrayList;

/**
 * Пакет, синхронизирующий некоторый поля {@link ExtendedPlayer}'а с ExtendedPlayer'ом на клиенте.
 */
public class PacketSyncPlayer implements IMessage {
    private ArrayList<ItemStack> skills;

    /**
     * Необходимый пустой публичный конструктор
     */
    public PacketSyncPlayer() {}

    public PacketSyncPlayer(ArrayList<ItemStack> skills) {
        this.skills = skills;
    }

    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        // Читаем размер списка
        int skillsSize = ByteBufUtils.readVarShort(buf);

        // Восстанавливаем список из ByteBuf
        skills = new ArrayList<ItemStack>();
        for (int i = 0; i < skillsSize; i++) {
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(ByteBufUtils.readTag(buf));
            skills.add(itemStack);
        }

    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, skills.size()); // Записываем размер списка
        for (ItemStack skill : skills) { // и сам список
            NBTTagCompound NBTSkillItem = new NBTTagCompound();
            skill.writeToNBT(NBTSkillItem);
            ByteBufUtils.writeTag(buf, NBTSkillItem);
        }
    }

    /**
     * Обработчик сообщения {@link PacketOpenRSStatsInventory}
     */
    public static class MessageHandler implements IMessageHandler<PacketSyncPlayer, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncPlayer message, MessageContext ctx) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            extendedPlayer.skillsInventory.setNewSkills(message.skills);
            extendedPlayer.updateParams();
            return null;
        }
    }
}
