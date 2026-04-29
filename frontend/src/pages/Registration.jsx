import { useState } from "react";
import ApprovalRequests from "../components/ApprovalRequests";
import FaceRegistration from "../components/FaceRegistration";
import Layout from "../components/Layout";

export default function Registration() {
  const [activeTab, setActiveTab] = useState("face");

  return (
    <Layout title="Registration" subtitle="Register student faces and review account approval requests.">
      <div className="stacked-page">
        <div className="tab-switcher">
          <button
            className={activeTab === "face" ? "tab-button active" : "tab-button"}
            type="button"
            onClick={() => setActiveTab("face")}
          >
            Face Registration
          </button>

          <button
            className={activeTab === "approval" ? "tab-button active" : "tab-button"}
            type="button"
            onClick={() => setActiveTab("approval")}
          >
            Approval Requests
          </button>
        </div>

        <div className="tab-content">
          {activeTab === "face" && <FaceRegistration activeTab={activeTab} />}
          {activeTab === "approval" && <ApprovalRequests activeTab={activeTab} />}
        </div>
      </div>
    </Layout>
  );
}
