package com.github.kpavlov.jreactive8583.example;

import com.github.kpavlov.jreactive8583.IsoMessageListener;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ClientServerIT extends AbstractIT {

    private volatile IsoMessage capturedRequest;

    @Before
    public void beforeTest() {
        server.addMessageListener(new IsoMessageListener<IsoMessage>() {

            @Override
            public boolean applies(IsoMessage isoMessage) {
                return isoMessage.getType() ==  0x200;
            }

            @Override
            public boolean onMessage(ChannelHandlerContext ctx, IsoMessage isoMessage) {
                capturedRequest = isoMessage;
                final IsoMessage response = server.getIsoMessageFactory().createResponse(isoMessage);
                response.setField(39, IsoType.ALPHA.value("00", 2));
                ctx.writeAndFlush(response);
                return false;
            }
        });
    }

    @Test
    public void testConnected() throws Exception {
        TestUtil.waitFor("server started", server::isStarted);
        TestUtil.waitFor("client connected", client::isConnected);

        final IsoMessage isoMessage = client.getIsoMessageFactory().newMessage(0x0200);
        isoMessage.setField(2, IsoType.LLVAR.value("asdfasdfasdfasdf", 16));
        isoMessage.setField(3, IsoType.NUMERIC.value("123456", 6));
        isoMessage.setField(4, IsoType.AMOUNT.value("2500000", 12));
        isoMessage.setField(7, IsoType.DATE10.value("1025050413"));
        isoMessage.setField(11, IsoType.NUMERIC.value("123456", 6));
        isoMessage.setField(12, IsoType.TIME.value("050504"));
        isoMessage.setField(13, IsoType.DATE4.value("1025"));
        isoMessage.setField(15, IsoType.DATE4.value("1025"));
        isoMessage.setField(18, IsoType.NUMERIC.value("1234", 4));
        isoMessage.setField(32, IsoType.LLVAR.value("aaaa",4));
        isoMessage.setField(37, IsoType.NUMERIC.value("123456789012",12));
        isoMessage.setField(41, IsoType.ALPHA.value("a1a1a1a1a1a1a1a1",16));
        isoMessage.setField(47, IsoType.LLLVAR.value("yXRsKiLNaqrGekQCbpRYfSdCjPNhteKfARklTbGeNolkXVkqrfuVRYCRObPuZNYJEVosukDQnBpzAxGXahWzdNlPjyuHocvnsKSobkfvGYlvyCwxgIvxWaVNBBcWLzSxVnlNQZYgpLFCVmajDBfTgZNsmETbVlTFFrFilOzMltQZwXjByMwTLKqwbPRCxcUaFtXpZhOiSLsIftAjti",210));
        isoMessage.setField(48, IsoType.LLLVAR.value("yXRsKiLNaqrGekQCbpRYfSdCjPNhteKfARklTbGeNolkXVkqrfuVRYCRObPuZNYJEVosukDQnBpzAxGXahWzdNlPjyuHocvnsKSobkfvGYlvyCwxgIvxWaVNBBcWLzSxVnlNQZYgpLFCVmajDBfTgZNsmETbVlTFFrFilOzMltQZwXjByMwTLKqwbPRCxcUaFtXpZhOiSLsIftAjti",210));
        isoMessage.setField(49, IsoType.NUMERIC.value("840",3));
        isoMessage.setField(63, IsoType.LLLVAR.value("aaaaaa",6));

        System.out.println(isoMessage.debugString());
        client.send(isoMessage);

        TestUtil.waitFor("capture request received", ()->(capturedRequest != null));

        assertThat("fin request", capturedRequest, notNullValue());
        assertThat("fin request", capturedRequest.debugString(), equalTo(isoMessage.debugString()));
    }


}
