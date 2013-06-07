package plugin.action;

import com.cedarsoft.serialization.generator.intellij.JacksonSerializerGenerator;
import com.cedarsoft.serialization.generator.intellij.SerializerResolver;
import com.cedarsoft.serialization.generator.intellij.model.SerializerModel;
import com.cedarsoft.serialization.generator.intellij.model.SerializerModelFactory;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.MultiFileTestCase;
import org.junit.*;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class JacksonSerializerGeneratorTest extends MultiFileTestCase {
  @Override
  protected Sdk getTestProjectJdk() {
    return new Jdk17MockProjectDescriptor().getSdk();
  }

  @Override
  protected String getTestDataPath() {
    return new File( "testData" ).getAbsolutePath();
  }

  @Override
  protected String getTestRoot() {
    String className = getClass().getName();

    int lastIndex = className.lastIndexOf( '.' );
    String relevantName = className.substring( lastIndex + 1 );

    return "/" + relevantName + "/";
  }

  @Test
  public void testSimple() throws Throwable {
    doTest( new PerformAction() {
      @Override
      public void performAction( VirtualFile rootDir, VirtualFile rootAfter ) throws Exception {
        PsiClass simple = myJavaFacade.findClass( getTestName( false ), GlobalSearchScope.allScope( getProject() ) );
        assertThat( simple ).isNotNull();
        assertThat( simple.getQualifiedName() ).isEqualTo( getTestName( false ) );

        SerializerModelFactory serializerModelFactory = new SerializerModelFactory( new SerializerResolver( getProject() ), JavaCodeStyleManager.getInstance( getProject() ) );
        SerializerModel model = serializerModelFactory.create( simple, ImmutableList.of( simple.findFieldByName( "foo", false ) ) );

        JacksonSerializerGenerator generator = new JacksonSerializerGenerator( getProject() );
        PsiClass serializer = generator.generate( model );
        assertThat( serializer.getName() ).isEqualTo( "SimpleSerializer" );
      }
    } );
  }

  @Test
  public void testSetter() throws Throwable {
    doTest( new PerformAction() {
      @Override
      public void performAction( VirtualFile rootDir, VirtualFile rootAfter ) throws Exception {
        PsiClass simple = myJavaFacade.findClass( getTestName( false ), GlobalSearchScope.allScope( getProject() ) );
        assertThat( simple ).isNotNull();
        assertThat( simple.getQualifiedName() ).isEqualTo( getTestName( false ) );

        SerializerModelFactory serializerModelFactory = new SerializerModelFactory( new SerializerResolver( getProject() ), JavaCodeStyleManager.getInstance( getProject() ) );
        SerializerModel model = serializerModelFactory.create( simple, ImmutableList.of( simple.findFieldByName( "foo", false ) ) );

        JacksonSerializerGenerator generator = new JacksonSerializerGenerator( getProject() );
        PsiClass serializer = generator.generate( model );
        assertThat( serializer.getName() ).isEqualTo( "SetterSerializer" );
      }
    } );
  }

  @Test
  public void testPrimitives() throws Throwable {
    doTest( new PerformAction() {
      @Override
      public void performAction( VirtualFile rootDir, VirtualFile rootAfter ) throws Exception {
        PsiClass foo = myJavaFacade.findClass( getTestName( false ), GlobalSearchScope.allScope( getProject() ) );
        assertThat( foo ).isNotNull();
        assertThat( foo.getQualifiedName() ).isEqualTo( getTestName( false ) );

        SerializerModelFactory serializerModelFactory = new SerializerModelFactory( new SerializerResolver( getProject() ), JavaCodeStyleManager.getInstance( getProject() ) );
        SerializerModel model = serializerModelFactory.create( foo, ImmutableList.<PsiField>copyOf( foo.getAllFields() ) );

        JacksonSerializerGenerator generator = new JacksonSerializerGenerator( getProject() );
        PsiClass serializer = generator.generate( model );
        assertThat( serializer.getName() ).isEqualTo( "PrimitivesSerializer" );
      }
    } );
  }
}
