package jarcode.bungee.consoles;

import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface OutgoingHookCommand {
	public void handle(ProxiedPlayer player, Object[] args, ByteArrayDataOutput out);
}
