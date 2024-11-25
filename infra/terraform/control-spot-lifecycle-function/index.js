import {serviceClients, Session, cloudApi} from '@yandex-cloud/nodejs-sdk';

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

const RUNNING = 2
const STOPPED = 4

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

async function getInstance(instanceClient, instanceId) {
    return instanceClient.get(GetInstanceRequest.fromPartial({
        instanceId: instanceId,
    }));
}

async function startInstance(instanceClient, instanceId) {
    const state = await getInstance(instanceClient, instanceId);
    if (state.status !== STOPPED) {
        return
    }
    return instanceClient.start(StartInstanceRequest.fromPartial({
        instanceId: instanceId,
    }));
}

async function stopInstance(instanceClient, instanceId) {
    const state = await getInstance(instanceClient, instanceId);
    if (state.status !== RUNNING) {
        return
    }
    return instanceClient.stop(StopInstanceRequest.fromPartial({
        instanceId: instanceId,
    }));
}
