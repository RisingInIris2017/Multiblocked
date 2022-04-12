package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.SimpleCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.common.capability.ItemMultiblockCapability;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCapabilityTrait extends SimpleCapabilityTrait {
    private ItemStackHandler handler;

    public ItemCapabilityTrait() {
        super(ItemMultiblockCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonArray();
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int size = jsonArray.size();
        handler = new ItemStackHandler(size);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        handler.deserializeNBT(compound.getCompoundTag("_"));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("_", handler.serializeNBT());
    }

    @Override
    public void createUI(WidgetGroup group, EntityPlayer player) {
        super.createUI(group, player);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                group.addWidget(new SlotWidget(new ProxyItemHandler(handler, guiIO), i, x[i], y[i], guiIO[i] == IO.BOTH || guiIO[i] == IO.OUT, guiIO[i] == IO.BOTH || guiIO[i] == IO.IN));
            }
        }
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ProxyItemHandler(handler, capabilityIO)) : null;
    }

    private static class ProxyItemHandler implements IItemHandler, IItemHandlerModifiable {
        public ItemStackHandler proxy;
        public IO[] ios;

        public ProxyItemHandler(ItemStackHandler proxy, IO[] ios) {
            this.proxy = proxy;
            this.ios = ios;
        }

        @Override
        public int getSlots() {
            return proxy.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return proxy.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            IO io = ios[slot];
            if (io == IO.BOTH || io == IO.IN) {
                return proxy.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IO io = ios[slot];
            if (io == IO.BOTH || io == IO.OUT) {
                return proxy.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return proxy.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return proxy.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            proxy.setStackInSlot(slot, stack);
        }
    }

}
