package com.valstro.hometask;


import com.google.gson.Gson;
import com.valstro.hometask.client.SocketIoClient;
import com.valstro.hometask.transaction.SearchTxn;
import com.valstro.hometask.transaction.Transaction;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ClientTests {

    private static final Gson GSON = new Gson();


    @InjectMocks
    SocketIoClient client = new SocketIoClient();

    @Mock
    Socket socket;

    @Test
    public void successfulSingleLineTransaction() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object obj = invocationOnMock.getArgument(1);
                if(obj instanceof Emitter.Listener) {
                    ((Emitter.Listener)obj).call("{\"films\":\"A New Hope, The Empire Strikes Back, Return of the Jedi, The Phantom Menace, Attack of the Clones, Revenge of the Sith\",\"resultCount\":1,\"name\":\"R2-D2\",\"page\":1}");
                }
                return null;
            }
        }).when(socket).on(ArgumentMatchers.anyString(), ArgumentMatchers.any(Emitter.Listener.class));
        when(socket.emit("search","{\"query\":\"luke\"}")).thenReturn(null);
        Transaction t = new SearchTxn("luke");
        client.executeTransaction(t);
        Assertions.assertTrue(t.getResult() != null);
    }

    @Test
    public void successfulMultiLineTransaction() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object obj = invocationOnMock.getArgument(1);
                if(obj instanceof Emitter.Listener) {
                    ((Emitter.Listener)obj).call("{\"films\":\"A New Hope, The Empire Strikes Back, Return of the Jedi, The Phantom Menace, Attack of the Clones, Revenge of the Sith\",\"resultCount\":3,\"name\":\"R2-D2\",\"page\":1}");
                    ((Emitter.Listener)obj).call("{\"films\":\"A New Hope, The Empire Strikes Back, Return of the Jedi, The Phantom Menace, Attack of the Clones, Revenge of the Sith\",\"resultCount\":3,\"name\":\"C3-PO\",\"page\":2}");
                    ((Emitter.Listener)obj).call("{\"films\":\"A New Hope, The Empire Strikes Back, Return of the Jedi, The Phantom Menace, Attack of the Clones, Revenge of the Sith\",\"resultCount\":3,\"name\":\"HAL\",\"page\":3}");
                }
                return null;
            }
        }).when(socket).on(ArgumentMatchers.anyString(), ArgumentMatchers.any(Emitter.Listener.class));
        when(socket.emit("search","{\"query\":\"luke\"}")).thenReturn(null);
        Transaction t = new SearchTxn("luke");
        client.executeTransaction(t);
        Assertions.assertTrue(t.getResult() != null);
    }

    @Test
    public void timedOutTransaction() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object obj = invocationOnMock.getArgument(1);
                if(obj instanceof Emitter.Listener) {
                    ((Emitter.Listener)obj).call("{\"films\":\"A New Hope, The Empire Strikes Back, Return of the Jedi, The Phantom Menace, Attack of the Clones, Revenge of the Sith\",\"resultCount\":3,\"name\":\"R2-D2\",\"page\":1}");
                }
                return null;
            }
        }).when(socket).on(ArgumentMatchers.anyString(), ArgumentMatchers.any(Emitter.Listener.class));
        when(socket.emit("search","{\"query\":\"luke\"}")).thenReturn(null);
        Transaction t = new SearchTxn("luke",2000);
        client.executeTransaction(t);
        Assertions.assertTrue(t.getStatus() == Transaction.Status.FAILED, "Transaction should fail");
        Assertions.assertEquals(t.getError(),"Transaction timed out.", "Invalid error message");
    }
}
