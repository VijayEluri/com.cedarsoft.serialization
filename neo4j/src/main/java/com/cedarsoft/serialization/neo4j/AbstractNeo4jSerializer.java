package com.cedarsoft.serialization.neo4j;

import com.cedarsoft.serialization.AbstractSerializer;
import com.cedarsoft.serialization.Serializer;
import com.cedarsoft.version.Version;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.version.VersionRange;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public abstract class AbstractNeo4jSerializer<T> extends AbstractSerializer<T, Node, Node, IOException, Node, Node> implements Serializer<T, Node, Node> {
  @Nonnull
  public static final String PROPERTY_FORMAT_VERSION = "formatVersion";
  @Nonnull 
  public static final String PROPERTY_TYPE = "type";

  @Nonnull
  private final String type; //$NON-NLS-1$

  protected AbstractNeo4jSerializer( @Nonnull String type, @Nonnull VersionRange formatVersionRange ) {
    super( formatVersionRange );
    this.type = type;
  }

  @Override
  public void serialize( @Nonnull T object, @Nonnull Node out ) throws IOException {
    serialize( out, object, getFormatVersion() );
  }

  @Override
  public void serialize( @Nonnull Node serializeTo, @Nonnull T object, @Nonnull Version formatVersion ) throws IOException, VersionException, IOException {
    serializeTo.setProperty( PROPERTY_TYPE, type );
    serializeTo.setProperty( PROPERTY_FORMAT_VERSION, getFormatVersion().toString() );
  }

  @Nonnull
  @Override
  public T deserialize( @Nonnull Node in ) throws IOException, VersionException {
    String readType = ( String ) in.getProperty( PROPERTY_TYPE );

    try {
      verifyType( readType );
    } catch ( InvalidTypeException e ) {
      throw new IOException( "Could not parse due to " + e.getMessage(), e );
    }

    Version version = Version.parse( ( String ) in.getProperty( PROPERTY_FORMAT_VERSION ) );
    verifyVersionReadable( version );

    return deserialize( in, version );
  }

  protected void verifyType( @Nonnull String readType ) throws InvalidTypeException {
    if ( !this.type.equals( readType ) ) {//$NON-NLS-1$
      throw new InvalidTypeException( readType, this.type );
    }
  }

  public <T> void serializeWithRelationships( @Nonnull Iterable<? extends T> objects, @Nonnull Class<T> type, @Nonnull Node node, @Nonnull RelationshipType relationshipType, @Nonnull Version formatVersion ) throws IOException {
    for ( T object : objects ) {
      serializeWithRelationship( object, type, node, relationshipType, formatVersion );
    }
  }

  /**
   * Serializes the given object using a relation
   *
   * @param object           the object that is serialized
   * @param type             the type
   * @param node             the (current) node that is the start for the relationship
   * @param relationshipType the type of the relationship
   * @param formatVersion    the format version
   * @param <T>              the type
   * @throws IOException
   */
  public <T> void serializeWithRelationship( @Nonnull T object, @Nonnull Class<T> type, @Nonnull Node node, @Nonnull RelationshipType relationshipType, @Nonnull Version formatVersion ) throws IOException {
    Node targetNode = node.getGraphDatabase().createNode();
    Relationship relationshipTo = node.createRelationshipTo( targetNode, relationshipType );
    serialize( object, type, targetNode, formatVersion );
  }

  @Nonnull
  public <T> T deserializeWithRelationship( @Nonnull Class<T> type, @Nonnull RelationshipType relationshipType, @Nonnull Node node, @Nonnull Version formatVersion ) throws IOException {
    @Nullable Relationship relationship = node.getSingleRelationship( relationshipType, Direction.OUTGOING );
    assert relationship != null;
    return deserialize( type, formatVersion, relationship.getEndNode() );
  }

  @Nonnull
  public <T> List<? extends T> deserializeWithRelationships( @Nonnull Class<T> type, @Nonnull RelationshipType relationshipType, @Nonnull Node node, @Nonnull Version formatVersion ) throws IOException {
    List<T> deserializedList = new ArrayList<>();
    for ( Relationship relationship : node.getRelationships( relationshipType, Direction.OUTGOING ) ) {
      deserializedList.add( deserialize( type, formatVersion, relationship.getEndNode() ) );
    }

    return deserializedList;
  }

  public static class InvalidTypeException extends Exception {
    @Nullable
    private final String type;
    @Nonnull
    private final String expected;

    public InvalidTypeException( @Nullable String type, @Nonnull String expected ) {
      super( "Invalid type. Was <" + type + "> but expected <" + expected + ">" );
      this.type = type;
      this.expected = expected;
    }

    @Nullable
    public String getType() {
      return type;
    }

    @Nonnull
    public String getExpected() {
      return expected;
    }
  }
}
