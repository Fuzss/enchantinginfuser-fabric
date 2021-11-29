package fuzs.enchantinginfuser.world.level.block.entity;

import fuzs.enchantinginfuser.registry.ModRegistry;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("NullableProblems")
public class InfuserBlockEntity extends EnchantmentTableBlockEntity implements WorldlyContainer, BlockEntityClientSerializable {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private LockCode code = LockCode.NO_LOCK;

    public InfuserBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(pWorldPosition, pBlockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        // set in super constructor, so just override the whole method
        return ModRegistry.INFUSER_BLOCK_ENTITY_TYPE;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventory.clear();
        ContainerHelper.loadAllItems(nbt, this.inventory);
        this.code = LockCode.fromTag(nbt);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        this.saveMetadataAndItems(compound);
        this.code.addToTag(compound);
        return compound;
    }

    private CompoundTag saveMetadataAndItems(CompoundTag compound) {
        super.save(compound);
        ContainerHelper.saveAllItems(compound, this.inventory, true);
        return compound;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.saveMetadataAndItems(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.inventory.clear();
        ContainerHelper.loadAllItems(tag, this.inventory);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public int getContainerSize() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < this.inventory.size() ? this.inventory.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.inventory, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < this.inventory.size()) {
            this.inventory.set(index, stack);
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level != null && this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) > 64.0);
        }
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) {
            return this.inventory.get(0).isEmpty() && (stack.isEnchantable() || stack.getItem() instanceof BookItem);
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStackIn);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        // only allow extracting of enchantable item
        return index == 0 && (stack.isEnchanted() || stack.getItem() instanceof EnchantedBookItem);
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    public boolean canOpen(Player p_213904_1_) {
        return BaseContainerBlockEntity.canUnlock(p_213904_1_, this.code, this.getDisplayName());
    }
}
