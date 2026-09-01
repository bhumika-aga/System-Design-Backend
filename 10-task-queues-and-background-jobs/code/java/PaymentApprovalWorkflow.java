// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 21 Human-in-the-loop
// Java 21 / Spring Boot 3.3

package com.example.tasks.workflow;

@WorkflowInterface
interface PaymentApprovalWorkflow {

    @WorkflowMethod
    void run(String orderId);

    // An external event that can wake this workflow up.
    @SignalMethod
    void approve(boolean approved);
}

class PaymentApprovalWorkflowImpl implements PaymentApprovalWorkflow {

    private final PaymentActivities activities = Workflow.newActivityStub(PaymentActivities.class, OPTIONS);

    private Boolean approved; // written by the signal below

    @Override
    public void run(String orderId) {
        activities.preparePayment(orderId);

        // Wait up to 24 hours. Temporal burns no CPU and holds no
        // memory while this waits: the workflow is not running.
        boolean signalled = Workflow.await(
                Duration.ofHours(24), () -> approved != null);

        if (!signalled || !approved) {
            activities.refundPayment(orderId); // saga: compensate
            return;
        }

        activities.finalizePayment(orderId);
    }

    @Override
    public void approve(boolean approved) {
        this.approved = approved;
    }
}

@RestController
class PaymentApprovalController {

    // Sending the signal from an HTTP handler:
    @PostMapping("/orders/{id}/approve")
    void approve(@PathVariable String id) {
        PaymentApprovalWorkflow workflow = temporal.newWorkflowStub(
                PaymentApprovalWorkflow.class, id);

        workflow.approve(true); // returns once Temporal records it
    }
}
