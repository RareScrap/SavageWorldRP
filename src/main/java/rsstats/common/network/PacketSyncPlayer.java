package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.StatsInventory;

import java.util.ArrayList;

/**
 * Пакет, синхронизирующий некоторый поля и инвентари {@link ExtendedPlayer}'а с ExtendedPlayer'ом на клиенте.
 */
public class PacketSyncPlayer implements IMessage {
    private static int BUFFER_INT_SIZE = 1;

    /** Объект, хранящий распакованне статы */
    private ItemStack[] stats;
    /** Объект, хранящий распакованне скиллы */
    private ArrayList<ItemStack> skills;
    /** Объект, хранящий распакованный уровень игрока */
    private int lvl;

    /**
     * Необходимый пустой публичный конструктор
     */
    public PacketSyncPlayer() {}

    public PacketSyncPlayer(ItemStack[] stats, ArrayList<ItemStack> skills, int lvl) {
        this.stats = stats;
        this.skills = skills;
        this.lvl = lvl;
    }

    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        // Читаем размер списка статов
        int statsSize = ByteBufUtils.readVarShort(buf);
        // Восстанавливаем список статов из ByteBuf
        stats = new ItemStack[StatsInventory.INV_SIZE];
        for (int i = 0; i < statsSize; i++) {
                ItemStack itemStack = ItemStack.loadItemStackFromNBT(ByteBufUtils.readTag(buf));
                // TODO: Т.к. в буфере может быть пустой ItemStack, я боюсь что может возникнуть ситуация, что игра интерпретирует пустой стак как пустой предмет, а не как свободное место. Следует ли мне заменять пустые стаки на null? Это нужно хорошенько проверить
                stats[i] = itemStack;
        }

        // Читаем размер списка скиллов
        int skillsSize = ByteBufUtils.readVarShort(buf);
        // Восстанавливаем список скиллов из ByteBuf
        skills = new ArrayList<ItemStack>();
        for (int i = 0; i < skillsSize; i++) {
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(ByteBufUtils.readTag(buf));
            skills.add(itemStack);
        }

        lvl = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, stats.length); // Записываем размер списка статов
        for (ItemStack stat : stats) { // А теперь записываем сам список
                NBTTagCompound NBTSkillItem = new NBTTagCompound();
                if (stat != null)
                    stat.writeToNBT(NBTSkillItem); // В случае, если stat == null, мы запишем в buf пустой стак
                ByteBufUtils.writeTag(buf, NBTSkillItem);
        }

        ByteBufUtils.writeVarShort(buf, skills.size()); // Записываем размер списка скиллов
        for (ItemStack skill : skills) { // и сам список
            NBTTagCompound NBTSkillItem = new NBTTagCompound();
            skill.writeToNBT(NBTSkillItem); // TODO: Элемент разве не может быть null?
            ByteBufUtils.writeTag(buf, NBTSkillItem);
        }

        ByteBufUtils.writeVarInt(buf, lvl, BUFFER_INT_SIZE);
    }

    /**
     * Обработчик сообщения {@link PacketOpenRSStatsInventory}
     */
    public static class MessageHandler implements IMessageHandler<PacketSyncPlayer, IMessage> {
        @Override
        @SideOnly(Side.CLIENT) // Для использования клиенских классов при регистрации пакета на серве
        public IMessage onMessage(PacketSyncPlayer message, MessageContext ctx) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            extendedPlayer.statsInventory.setNewStats(message.stats);
            extendedPlayer.skillsInventory.setNewSkills(message.skills);
            extendedPlayer.setLvl(message.lvl);
            extendedPlayer.updateParams();
            return null;
        }
    }
}
