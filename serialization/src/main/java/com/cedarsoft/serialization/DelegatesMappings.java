package com.cedarsoft.serialization;

import com.cedarsoft.Version;
import com.cedarsoft.VersionRange;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains several delegates mappings
 *
 * @param <S> the object to serialize to (e.g. a dom element or stream)
 * @param <D> the object to deserialize from ((e.g. a dom element or stream)
 * @param <E> the exception that might be thrown
 */
public class DelegatesMappings<S, D, E extends Throwable> {
  @NotNull
  private final VersionRange versionRange;

  @NotNull
  private final Map<Class<?>, DelegateMapping> mappings = new HashMap<Class<?>, DelegateMapping>();
  @NotNull
  private final Map<Class<?>, Serializer<?>> serializers = new HashMap<Class<?>, Serializer<?>>();

  public DelegatesMappings( @NotNull VersionRange versionRange ) {
    this.versionRange = versionRange;
  }

  @NotNull
  public Map<? extends Class<?>, ? extends DelegateMapping> getMappings() {
    return Collections.unmodifiableMap( mappings );
  }

  @NotNull
  public <T> FluentFactory<T> add( @NotNull PluggableSerializer<? super T, S, D, E> serializer ) {
    return new FluentFactory( serializer );
  }

  @NotNull
  public <T> Version resolveVersion( @NotNull Class<? extends T> key, @NotNull Version version ) {
    return getMapping( key ).resolveVersion( version );
  }

  @NotNull
  public DelegateMapping getMapping( @NotNull Class<?> key ) {
    DelegateMapping mapping = mappings.get( key );
    if ( mapping == null ) {
      throw new IllegalArgumentException( "No mapping found for <" + key + ">" );
    }
    return mapping;
  }

  public <T> void serialize( @NotNull Class<T> type, @NotNull S outputElement, @NotNull T object ) throws E, IOException {
    PluggableSerializer<? super T, S, D, E> serializer = getSerializer( type );
    Version version = resolveVersion( Object.class, serializer.getFormatVersion() );

    serializer.serialize( outputElement, object );
  }

  @NotNull
  public <T> PluggableSerializer<? super T, S, D, E> getSerializer( @NotNull Class<T> type ) {
    return ( PluggableSerializer<? super T, S, D, E> ) serializers.get( type );
  }

  @NotNull
  public <T> T deserialize( @NotNull Class<T> type, @NotNull Version formatVersion, @NotNull D deserializeFrom ) throws E, IOException {
    PluggableSerializer<? super T, S, D, E> serializer = getSerializer( type );
    return type.cast( serializer.deserialize( deserializeFrom, resolveVersion( type, formatVersion ) ) );
  }

  public class FluentFactory<T> {
    @NotNull
    private final PluggableSerializer<? super T, S, D, E> serializer;

    public FluentFactory( @NotNull PluggableSerializer<? super T, S, D, E> serializer ) {
      this.serializer = serializer;
    }

    @NotNull
    public DelegateMapping responsibleFor( @NotNull Class<? extends T> key ) {
      if ( mappings.containsKey( key ) ) {
        throw new IllegalArgumentException( "A serializer for the key <" + key + "> has still been added" );
      }

      DelegateMapping mapping = new DelegateMapping( versionRange, serializer.getFormatVersionRange() );
      mappings.put( key, mapping );
      serializers.put( key, serializer );
      return mapping;
    }
  }
}

/*
Ascii-Art sample:
              Window        Door        Other
----------------------------------------------
1.0.0         1.0.0         1.0.0       1.2.1
1.0.1           |             |         1.2.2
1.0.2           |             |         1.3.0
1.1.0           |             |         1.3.1
1.1.1           |             |         1.4.0
1.5.0         2.0.0           |           |
2.0.0           |             |         2.0.0
----------------------------------------------
2.0.0         2.0.0         1.0.0       2.0.0
*/