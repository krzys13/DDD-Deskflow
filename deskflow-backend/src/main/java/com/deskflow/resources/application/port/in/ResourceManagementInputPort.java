package com.deskflow.resources.application.port.in;

import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.resources.application.model.ResourceView;

import java.util.List;
import java.util.Optional;

public interface ResourceManagementInputPort {

    ResourceId addResource(ResourceType type, String name, String building, int floor, String number);

    void updateResource(ResourceId resourceId, String name, String building, int floor, String number);

    void markUnavailable(ResourceId resourceId);

    void markAvailable(ResourceId resourceId);

    void deactivateResource(ResourceId resourceId);

    Optional<ResourceView> getResource(ResourceId resourceId);

    List<ResourceView> getActiveResources();
}
