package azzy.fabric.lookingglass.blockentity;

import azzy.fabric.incubus_core.be.BlockEntityMover;
import azzy.fabric.lookingglass.block.LookingGlassBlocks;
import azzy.fabric.lookingglass.render.TesseractRenderable;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.tuple.Triple;

public class BlockTesseractEntity extends BlockEntity implements BlockEntityClientSerializable, TesseractRenderable {

    private static final ItemStack core = new ItemStack(Items.ENDER_EYE);
    private BlockPos movePos;

    public BlockTesseractEntity(BlockPos pos, BlockState state) {
        super(LookingGlassBlocks.BLOCK_TESSERACT_ENTITY, pos, state);
    }

    public void moveBlock(Direction direction) {
        if(movePos != null) {
            BlockState moveState = world.getBlockState(movePos);
            BlockPos oldPos = movePos;
            BlockPos target = movePos.offset(direction);
            if(world.isAir(movePos)) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, oldPos.getX() + 0.5, oldPos.getY() + 0.5, oldPos.getZ() + 0.5, 1, 0, 0, 0, 0);
                ((ServerWorld) world).spawnParticles(ParticleTypes.DRAGON_BREATH, movePos.getX() + 0.5, movePos.getY() + 0.5, movePos.getZ() + 0.5, 10 + world.getRandom().nextInt(10), 0, 0, 0, 0.04);
                movePos = null;
                sync();
                return;
            }
            BlockState targetState = world.getBlockState(target);
            Block moveBlock = moveState.getBlock();
            float targetHardness = targetState.getHardness(world, target);
            float moveHardness = moveState.getHardness(world, movePos);
            PushAction action = PushAction.NONE;

            //Do nothing if hardness is the same
            if(world.isAir(target) && !(moveHardness < 0F)) {
                action = PushAction.MOVE;
            }
            else if(moveBlock == LookingGlassBlocks.INTERMINAL_CORE || targetState.isOf(LookingGlassBlocks.INTERMINAL_CORE) || moveHardness < 0F) {
                action = PushAction.TRANSFER;
            }
            else if(moveBlock == LookingGlassBlocks.ANNULATION_CORE_1A || (moveHardness > targetHardness && targetHardness > 0f)) {
                action = PushAction.TARBREAK;
            }
            else if(targetHardness < 0F || moveHardness < targetHardness) {
                action = PushAction.SELFBREAK;
            }
            switch (action) {
                case TRANSFER: {
                    movePos = movePos.offset(direction);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.DRAGON_BREATH, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 10 + world.getRandom().nextInt(10), 0, 0, 0, 0.08);
                    sync();
                    return;
                }
                case MOVE: {
                    BlockEntity entity = world.getBlockEntity(movePos);
                    if(entity != null) {
                        BlockEntityMover.directEntityMove((ServerWorld) world, movePos, target);
                    }
                    else {
                        world.setBlockState(target, world.getBlockState(movePos));
                        world.setBlockState(movePos, Blocks.AIR.getDefaultState());
                    }
                    break;
                }
                case TARBREAK:{
                    world.breakBlock(target, true);
                    BlockEntity entity = world.getBlockEntity(movePos);
                    if(entity != null) {
                        BlockEntityMover.directEntityMove((ServerWorld) world, movePos, target);
                    }
                    else {
                        world.setBlockState(target, world.getBlockState(movePos));
                        world.setBlockState(movePos, Blocks.AIR.getDefaultState());
                    }
                    break;
                }
                case SELFBREAK: {
                    world.breakBlock(movePos, true);
                }
                case NONE: return;
            }
            ((ServerWorld) world).spawnParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, oldPos.getX() + 0.5, oldPos.getY() + 0.5, oldPos.getZ() + 0.5, 1, 0, 0, 0, 0);
            ((ServerWorld) world).spawnParticles(ParticleTypes.DRAGON_BREATH, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 10 + world.getRandom().nextInt(10), 0, 0, 0, 0.08);
            movePos = movePos.offset(direction);
            sync();
            return;
        }
        sync();
    }

    public boolean setTarget(long encodedPos) {
        BlockPos pos = BlockPos.fromLong(encodedPos);
        if (pos != this.pos && world.isInBuildLimit(pos)) {
            movePos = pos;
            return true;
        }
        return false;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        if(movePos != null)
            tag.putLong("target", movePos.asLong());
        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        if(tag.contains("target"))
            movePos = BlockPos.fromLong(tag.getLong("target"));
        super.readNbt(tag);
    }

    @Override
    public boolean shouldRenderCore() {
        return movePos != null;
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    @Override
    public ItemStack getCoreItem() {
        return core;
    }

    @Override
    public Triple<Integer, Integer, Integer> getColor() {
        return Triple.of(255, 255, 255);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        if(movePos != null)
            tag.putLong("target", movePos.asLong());
        return tag;
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        if(tag.contains("target"))
            movePos = BlockPos.fromLong(tag.getLong("target"));
    }

    static {
        core.addEnchantment(Enchantments.SHARPNESS, 1);
    }

    private enum PushAction {
        NONE,
        TRANSFER,
        MOVE,
        TARBREAK,
        SELFBREAK
    }
}
