package org.apereo.cas.util.gen;

import java.security.SecureRandom;

/**
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Timur Duehr

 * @since 5.2.0
 */
abstract public class AbstractRandomStringGenerator implements RandomStringGenerator{
    /** An instance of secure random to ensure randomness is secure. */
    protected final SecureRandom randomizer = new SecureRandom();
    protected final int defaultLength;

    /**
     * Instantiates a new default random string generator
     * with length set to {@link RandomStringGenerator#DEFAULT_LENGTH}.
     */
    public AbstractRandomStringGenerator() {
        this.defaultLength = DEFAULT_LENGTH;
    }

    /**
     * Instantiates a new default random string generator.
     *
     * @param defaultLength the max random length
     */
    public AbstractRandomStringGenerator(final int defaultLength) {
        this.defaultLength = defaultLength;
    }

    @Override
    public int getDefaultLength() {
        return defaultLength;
    }

    @Override
    public String getNewString(int size) {
        final byte[] random = getNewStringAsBytes(size);
        return this.convertBytesToString(random);
    }

    /**
     * Converts byte[] to String by simple cast. Subclasses should override.
     *
     * @param random raw bytes
     * @return a converted String
     */
    protected String convertBytesToString(final byte[] random) {
        return new String(random);
    }


    @Override
    public String getNewString() {
        return getNewString(this.getDefaultLength());
    }

    @Override
    public byte[] getNewStringAsBytes(final int size) {
        final byte[] random = new byte[size];
        this.randomizer.nextBytes(random);
        return random;
    }

    @Override
    public byte[] getNewStringAsBytes() {
        return this.getNewStringAsBytes(this.getDefaultLength());
    }
}
