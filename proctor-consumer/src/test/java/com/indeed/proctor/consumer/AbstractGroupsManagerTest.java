package com.indeed.proctor.consumer;

import com.indeed.proctor.common.Identifiers;
import com.indeed.proctor.common.Proctor;
import com.indeed.proctor.common.model.TestBucket;
import com.indeed.proctor.common.model.TestType;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.indeed.proctor.consumer.ProctorConsumerUtils.FORCE_GROUPS_PARAMETER;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AbstractGroupsManagerTest {

    @Test
    public void testDetermineBuckets() {

        final Proctor proctorMock = mock(Proctor.class);
        final Identifiers identifiers = Identifiers.of(TestType.ANONYMOUS_USER, "fooUser");
        final AbstractGroupsManager manager = new AbstractGroupsManager(() -> proctorMock) {
            @Override
            public Map<String, String> getProvidedContext() {
                return null;
            }

            @Override
            protected Map<String, TestBucket> getDefaultBucketValues() {
                return null;
            }
        };

        final HttpServletRequest httpRequestMock = mock(HttpServletRequest.class);
        final HttpServletResponse httpResponseMock = mock(HttpServletResponse.class);
        {
            // no force groups
            manager.determineBucketsInternal(
                    httpRequestMock, httpResponseMock, identifiers, emptyMap(), true);
            verify(httpRequestMock, times(1)).getContextPath();
            verify(httpRequestMock, times(1)).getHeader(anyString());
            verify(httpRequestMock, times(1)).getCookies();
            verify(httpRequestMock, times(1)).getParameter(anyString());
            verifyNoMoreInteractions(httpRequestMock, httpResponseMock);
            clearInvocations(httpRequestMock, httpResponseMock);
        }
        {
            // allow force groups = false
            when(httpRequestMock.getParameter(FORCE_GROUPS_PARAMETER)).thenReturn("foo1");
            manager.determineBucketsInternal(
                    httpRequestMock, httpResponseMock, identifiers, emptyMap(), false);
            verifyNoMoreInteractions(httpRequestMock, httpResponseMock);
            clearInvocations(httpRequestMock, httpResponseMock);
        }
        {
            // allow force groups = true
            manager.determineBucketsInternal(
                    httpRequestMock, httpResponseMock, identifiers, emptyMap(), true);
            verify(httpRequestMock, times(1)).getContextPath();
            verify(httpRequestMock, times(1)).getParameter(anyString());
            verify(httpResponseMock, times(1)).addCookie(isA(Cookie.class));
            verifyNoMoreInteractions(httpRequestMock, httpResponseMock);
            clearInvocations(httpRequestMock, httpResponseMock);
        }

    }
}
