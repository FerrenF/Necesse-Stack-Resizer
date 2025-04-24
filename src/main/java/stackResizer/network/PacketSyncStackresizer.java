package stackResizer.network;

import java.util.Collections;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import stackResizer.StackResizer;

public class PacketSyncStackresizer extends Packet {

	public final SyncType request;

	public PacketSyncStackresizer(byte[] data) {
		super(data);
		PacketReader reader = new PacketReader(this);
	
		boolean syncItemModifications = false;
		boolean syncCategoryModifications = false;
		boolean syncItemBlacklist = false;
		boolean syncCategoryBlacklist = false;
		boolean syncDefault = false;

		int length;
		String in;

		this.request = SyncType.values()[reader.getNextByteUnsigned()];

		if (request.equals(SyncType.ALL)) {
			syncItemModifications = true;
			syncCategoryModifications = true;
			syncItemBlacklist = true;
			syncCategoryBlacklist = true;
			syncDefault = true;
		}
		/*if (request.equals(SyncType.ITEM_MODIFICATIONS)) {
			syncItemModifications = true;
		}
		if (request.equals(SyncType.CATEGORY_MODIFICATIONS)) {
			syncCategoryModifications = true;
		}
		if (request.equals(SyncType.ITEM_BLACKLIST)) {
			syncItemBlacklist = true;
		}
		if (request.equals(SyncType.CATEGORY_BLACKLIST)) {
			syncCategoryBlacklist = true;
		}
		if (request.equals(SyncType.DEFAULT)) {
			syncDefault = true;
		}*/

		if (syncItemModifications) {
			length = reader.getNextInt();
			in = reader.getNextString();
			StackResizer.getCurrentSettings().setItemModifiersFromString(in);
		}

		if (syncCategoryModifications) {
			length = reader.getNextInt();
			in = reader.getNextString();
			StackResizer.getCurrentSettings().setCategoryModifiersFromString(in);
		}

		if (syncItemBlacklist) {
			length = reader.getNextInt();
			in = reader.getNextString();
			StackResizer.getCurrentSettings().setItemBlacklistFromString(in);
		}

		if (syncCategoryBlacklist) {
			length = reader.getNextInt();
			in = reader.getNextString();
			StackResizer.getCurrentSettings().setCategoryBlacklistFromString(in);
		}

		if (syncDefault) {
			int defaultMod = reader.getNextInt();
			StackResizer.getCurrentSettings().setDefaultModifier(defaultMod);
		}
	}

	public PacketSyncStackresizer(SyncType request) {
		this.request = request;

		PacketWriter writer = new PacketWriter(this);
	
		writer.putNextByteUnsigned(request.ordinal());
		boolean syncItemModifications = false;
		boolean syncCategoryModifications = false;
		boolean syncItemBlacklist = false;
		boolean syncCategoryBlacklist = false;
		boolean syncDefault = false;

		if (request.equals(SyncType.ALL)) {
			syncItemModifications = true;
			syncCategoryModifications = true;
			syncItemBlacklist = true;
			syncCategoryBlacklist = true;
			syncDefault = true;
		}
		/*if (request.equals(SyncType.ITEM_MODIFICATIONS)) {
			syncItemModifications = true;
		}
		if (request.equals(SyncType.CATEGORY_MODIFICATIONS)) {
			syncCategoryModifications = true;
		}
		if (request.equals(SyncType.ITEM_BLACKLIST)) {
			syncItemBlacklist = true;
		}
		if (request.equals(SyncType.CATEGORY_BLACKLIST)) {
			syncCategoryBlacklist = true;
		}
		if (request.equals(SyncType.DEFAULT)) {
			syncDefault = true;
		}*/

		if (syncItemModifications) {
			String out = StackResizer.getCurrentSettings().itemModifierListToString();
			writer.putNextInt(out.length());
			writer.putNextString(out);
		}

		if (syncCategoryModifications) {
			String out = StackResizer.getCurrentSettings().categoryModifierListToString();
			writer.putNextInt(out.length());
			writer.putNextString(out);
		}

		if (syncItemBlacklist) {
			String out = StackResizer.getCurrentSettings().itemBlacklistToString();
			writer.putNextInt(out.length());
			writer.putNextString(out);
		}

		if (syncCategoryBlacklist) {
			String out = StackResizer.getCurrentSettings().categoryBlacklistToString();
			writer.putNextInt(out.length());
			writer.putNextString(out);
		}

		if (syncDefault) {
			writer.putNextInt(StackResizer.getCurrentSettings().default_stackSize_modifier);
		}

		
	}

	public void processServer(NetworkPacket packet, Server server, ServerClient client) {
		// Broadcast to clients if this was from a client → server
		
		
	}

	// Optional client handler
	public void processClient(NetworkPacket packet) {
		// Already handled in constructor, so likely nothing needed here
	}
}
