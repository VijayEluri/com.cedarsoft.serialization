import org.jetbrains.annotations.NotNull;

public class SetterSerializerVersionTest extends com.cedarsoft.serialization.test.utils.AbstractJsonVersionTest2<Setter> {
    @NotNull
    @org.junit.experimental.theories.DataPoint
    public static final com.cedarsoft.serialization.test.utils.VersionEntry ENTRY1 = com.cedarsoft.serialization.test.utils.AbstractJsonVersionTest2.create(
            com.cedarsoft.version.Version.valueOf(1, 0, 0), SetterSerializerVersionTest.class.getResource("Setter_1.0.0_1.json"));

    @NotNull
    @Override
    protected com.cedarsoft.serialization.StreamSerializer<Setter> getSerializer() throws Exception {
        return com.google.inject.Guice.createInjector().getInstance(SetterSerializer.class);
    }

    @Override
    protected void verifyDeserialized(@NotNull Setter deserialized, @NotNull com.cedarsoft.version.Version version) {
        org.junit.Assert.assertNotNull(deserialized.getFoo());
    }
}