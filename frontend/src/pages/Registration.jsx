import { useState } from "react";
import ApprovalRequests from "../components/ApprovalRequests";
import FaceRegistration from "../components/FaceRegistration";
import Layout from "../components/Layout";

export default function Registration() {
  const [activeTab, setActiveTab] = useState("face");

  return (
    <Layout title="Registration" subtitle="Register student faces and review account approval requests.">
      <div className="grid gap-5">
        <div className="flex flex-wrap gap-3">
          <button
            className={`rounded-2xl border px-5 py-3 text-sm font-semibold transition ${
              activeTab === "face"
                ? "border-[#163f69] bg-[#163f69] text-white"
                : "border-[rgba(22,63,105,0.16)] bg-[rgba(255,255,255,0.78)] text-[#163f69]"
            }`}
            type="button"
            onClick={() => setActiveTab("face")}
          >
            Face Registration
          </button>

          <button
            className={`rounded-2xl border px-5 py-3 text-sm font-semibold transition ${
              activeTab === "approval"
                ? "border-[#163f69] bg-[#163f69] text-white"
                : "border-[rgba(22,63,105,0.16)] bg-[rgba(255,255,255,0.78)] text-[#163f69]"
            }`}
            type="button"
            onClick={() => setActiveTab("approval")}
          >
            Approval Requests
          </button>
        </div>

        <div>
          {activeTab === "face" && <FaceRegistration activeTab={activeTab} />}
          {activeTab === "approval" && <ApprovalRequests activeTab={activeTab} />}
        </div>
      </div>
    </Layout>
  );
}
