package site.siredvin.peripheralworks.mixins;

import com.google.common.io.BaseEncoding;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.util.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.siredvin.peripheralworks.DigestOutputStream;

import javax.annotation.Nullable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Cursed as hell, actually.
 * This applies fixes from <a href="https://github.com/cc-tweaked/CC-Tweaked/blob/mc-1.19.x/projects/common/src/main/java/dan200/computercraft/shared/util/NBTUtil.java">here</a>
 * as a mixin to util class for fixing sorting issue.
 * Is this cursed enough? I think so.
 */
@Mixin(NBTUtil.class)
public class NBTUtilMixin {

    @Final
    private static BaseEncoding ENCODING;

    @Inject(method="getNBTHash", at=@At("HEAD"), cancellable = true)
    private static void getNBTHash(@Nullable CompoundTag tag, CallbackInfoReturnable<String> cir) {
        if (tag == null) cir.setReturnValue(null);

        try {
            var digest = MessageDigest.getInstance("MD5");
            DataOutput output = new DataOutputStream(new DigestOutputStream(digest));
            writeNamedTag(output, "", assertNonNull(tag));
            var hash = digest.digest();
            cir.setReturnValue(ENCODING.encode(hash));
        } catch (NoSuchAlgorithmException | IOException e) {
            ComputerCraft.log.error("Cannot hash NBT", e);
            cir.setReturnValue(null);
        }
    }

    private static void writeNamedTag(DataOutput output, String name, Tag tag) throws IOException {
        output.writeByte(tag.getId());
        if (tag.getId() == 0) return;
        output.writeUTF(name);
        writeTag(output, tag);
    }

    private static <T> T assertNonNull(T value) {
        if (value == null) throw new NullPointerException("Well, that's really something, how did you even get here?");
        return value;
    }

    private static void writeTag(DataOutput output, Tag tag) throws IOException {
        if (tag instanceof CompoundTag compound) {
            var keys = compound.getAllKeys().toArray(new String[0]);
            Arrays.sort(keys);
            for (var key : keys) writeNamedTag(output, key, assertNonNull(compound.get(key)));

            output.writeByte(0);
        } else if (tag instanceof ListTag list) {
            output.writeByte(list.isEmpty() ? 0 : list.get(0).getId());
            output.writeInt(list.size());
            for (var value : list) writeTag(output, value);
        } else {
            tag.write(output);
        }
    }
}
