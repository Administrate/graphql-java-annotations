/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.*;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnhancedExecutionStrategy extends SimpleExecutionStrategy {

    private static final Logger log = LoggerFactory.getLogger(EnhancedExecutionStrategy.class);

    @Override
    protected ExecutionResult resolveField(ExecutionContext executionContext, GraphQLObjectType parentType, Object source, List<Field> fields) {
        GraphQLFieldDefinition fieldDef = getFieldDef(executionContext.getGraphQLSchema(), parentType, fields.get(0));
        if (fieldDef == null) return null;

        if (fieldDef.getName().contentEquals("clientMutationId")) {
            Field field = (Field) executionContext.getOperationDefinition().getSelectionSet().getSelections().get(0);
            ObjectValue value = (ObjectValue) field.getArguments().get(0).getValue();
            StringValue clientMutationIdVal = (StringValue) value.getObjectFields().stream().filter(f -> f.getName().contentEquals("clientMutationId")).findFirst().get().getValue();

            String clientMutationId = clientMutationIdVal.getValue();

            return completeValue(executionContext, fieldDef.getType(), fields, clientMutationId);
        } else {
            return super.resolveField(executionContext, parentType, source, fields);
        }
    }

    @Override
    protected ExecutionResult completeValue(ExecutionContext executionContext, GraphQLType fieldType, List<Field> fields, Object result) {
        if (result instanceof Optional) {
            return super.completeValue(executionContext, fieldType, fields, ((Optional) result).orElse(null));
        }
        return super.completeValue(executionContext, fieldType, fields, result);
    }
}
