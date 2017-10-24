package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.io.ByteBufferOutput;
import org.apereo.cas.memcached.kryo.serial.ZonedDateTimeSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link ZonedDateTimeSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(JUnit4.class)
public class ZonedDateTimeSerializerTests {
    
    @Test
    public void verifyTranscoderWorks() {
        final CasKryoPool pool = new CasKryoPool();
        try (CloseableKryo kryo = pool.borrow()) {
            final ZonedDateTimeSerializer transcoder = new ZonedDateTimeSerializer();
            final ByteBufferOutput output = new ByteBufferOutput(2048);
            transcoder.write(kryo, output, ZonedDateTime.now(ZoneOffset.UTC));
        }
        
    }
}
