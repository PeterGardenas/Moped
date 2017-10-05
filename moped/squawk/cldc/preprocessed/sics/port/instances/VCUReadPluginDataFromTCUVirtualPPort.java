package sics.port.instances;

import com.sun.squawk.VM;
import sics.port.EcuVirtualPPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VCUReadPluginDataFromTCUVirtualPPort extends EcuVirtualPPort {

	public VCUReadPluginDataFromTCUVirtualPPort(int id) {
		super(id);
	}

	@Override
	public Object deliver() {
		return null;
	}

	public Object deliver(int portId) {
		int pluginDataSize = VM.jnaReadPluginDataSizeFromTCU();
		// size should be larger than 4, cause the first 4 bytes represent
		// destination plug-in port id, the rest of bytes are value
		//VM.println("VCUReadPluginDataFromTCUVirtualPPort deliver " + portId + " " + pluginDataSize);
		if (pluginDataSize <= 4) {
			return null;
		} else {
			// the first 4 bytes represents destinationPortId
			byte[] destinatedPortIdBytes = new byte[4];
			for (int i = 0; i < 4; i++) {
				destinatedPortIdBytes[i] = VM.jnaReadPluginDataByteFromTCU(i);
			}
			int destinatedPortId = byteArrayToInt(destinatedPortIdBytes);
			if (destinatedPortId != portId) {
				return null;
			} else {
				int valLength = pluginDataSize - 4;
				byte[] valBytes = new byte[valLength];
				for (int i = 0; i < valLength ; i++) {
					valBytes[i] = VM.jnaReadPluginDataByteFromTCU(i+4);
				}
				String res = new String(valBytes);
				
				VM.jnaResetPluginDataSizeFromTCU();
				return res;
			}
		}
	}

}
