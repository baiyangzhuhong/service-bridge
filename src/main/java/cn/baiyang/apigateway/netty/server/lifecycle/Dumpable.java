package cn.baiyang.apigateway.netty.server.lifecycle;

import java.io.IOException;

public interface Dumpable {

	String dump();

	void dump(Appendable out, String indent) throws IOException;

}
