import {serviceClients, Session, cloudApi} from '@yandex-cloud/nodejs-sdk';
import {Instance_Status} from "@yandex-cloud/nodejs-sdk/dist/generated/yandex/cloud/compute/v1/instance";

const {
    compute: {
        instance_service: {
            ListInstancesRequest,
            GetInstanceRequest,
            StartInstanceRequest,
            StopInstanceRequest,
        },
    },
} = cloudApi;

const FOLDER_ID = process.env.FOLDER_ID;
const INSTANCE_ID = process.env.INSTANCE_ID;
const OAUTHTOKEN = process.env.OAUTHTOKEN;
const RUN_MODE = process.env.RUN_MODE;

export const handler = async function (event, context) {
    const session = new Session({oauthToken: OAUTHTOKEN});
    const instanceClient = session.client(serviceClients.InstanceServiceClient);
    await instanceClient.list(ListInstancesRequest.fromPartial({
        folderId: FOLDER_ID,
    }));

    switch (RUN_MODE) {
        case "starter":
            await startInstance(instanceClient, INSTANCE_ID);
            break;
        case "stopper":
            await stopInstance(instanceClient, INSTANCE_ID);
            break;
        default:
            return {
                statusCode: 400,
                body: {
                    message: `Invalid RUN_MODE: ${RUN_MODE}`,
                }
            }
    }

    return {
        statusCode: 200,
    };
};

async function getInstance(instanceClient: any, instanceId: string) {
    return instanceClient.get(GetInstanceRequest.fromPartial({
        instanceId: instanceId,
    }));
}

async function startInstance(instanceClient: any, instanceId: string) {
    const state = await getInstance(instanceClient, instanceId);
    if (state.status !== Instance_Status.STOPPED) {
        return
    }
    return instanceClient.start(StartInstanceRequest.fromPartial({
        instanceId: instanceId,
    }));
}

async function stopInstance(instanceClient: any, instanceId: string) {
    const state = await getInstance(instanceClient, instanceId);
    if (state.status !== Instance_Status.RUNNING) {
        return
    }
    return instanceClient.stop(StopInstanceRequest.fromPartial({
        instanceId: instanceId,
    }));
}
